#!/usr/bin/env python3

#
# Copyright (c) 2022 The Hyve B.V.
# This code is licensed under the GNU Affero General Public License (AGPL),
# version 3, or (at your option) any later version.
#

#
# This file is part of cBioPortal.
#
# cBioPortal is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

"""
Mutational signatures script. Create mutation matrices and calculate mutational
signatures from mutational data. For usage, run the script with the -h option.

Example:
    python3 mutationalSignatures.py --study-path /data/study1 --out-dir /data/out -t /app/tempoSig.R
      -s /cosmic/mut_signatures_SBS.txt -d /cosmic/mut_signatures_DBS.txt -i /cosmic/mut_signatures_ID.txt
"""

# imports
import argparse
import glob
import os
import re
import shutil
import subprocess

import os.path as osp
import pandas as pd
from SigProfilerMatrixGenerator import install as genInstall
from SigProfilerMatrixGenerator.scripts import SigProfilerMatrixGeneratorFunc as matGen


def parse_args() -> argparse.Namespace:
    """Parse command line arguments
    
    :return: parsed arguments
    """
    parser = argparse.ArgumentParser(
        description="Extract mutational signatures from a cBioPortal study/MAF/VCFs and generate output in cBioPortal format.",
    )
    input_group = parser.add_argument_group("input (mutually exclusive)")
    input_excl = input_group.add_mutually_exclusive_group(required=True)
    input_excl.add_argument("--study-path", metavar="./path/", dest="study_path", type=existing_file,
                            help="cBioPortal study path")
    input_excl.add_argument("--maf", metavar="file.maf", type=existing_file,
                            help="MAF file (--study-id required)")
    input_excl.add_argument("--vcf-folder", metavar="./path/", dest="vcf_folder", type=existing_file,
                            help="folder containing VCF files with sampleID as filename "
                                 "(--study-id and --genome required)")
    # REQUIRED ARGUMENTS
    required_group = parser.add_argument_group("required arguments")
    required_group.add_argument("-t", "--temposig-loc", dest="temposig_loc", metavar="temposig.R", type=existing_file,
                                required=True, help="location of 'tempoSig.R' script")
    required_group.add_argument("-o", "--out-dir", metavar="./path/", dest="out_dir", required=True,
                                type=lambda x: existing_file(x, create_dir=True), help="output directory")
    # SIGNATURE FILES
    sigs = parser.add_argument_group("signature files (at least one is required)")
    sigs.add_argument("-s", "--cosmic-sbs-file", dest="sbs_file", metavar="cosmic_SBS.txt",
                      type=existing_file, help="extract single base substitution signatures with the provided file")
    sigs.add_argument("-d", "--cosmic-dbs-file", dest="dbs_file", metavar="cosmic_DBS.txt",
                      type=existing_file, help="extract double base substitution signatures with the provided file")
    sigs.add_argument("-i", "--cosmic-id-file", dest="id_file", metavar="cosmic_ID.txt",
                      type=existing_file, help="extract insertion/deletion signatures with the provided file")
    # OPTIONAL DEPENDING ON INPUT
    optional_group = parser.add_argument_group("optional arguments")
    optional_group.add_argument("--study-id", metavar="studyid", dest="study_id", type=str,
                                help="study ID (used for metafile generation)")
    optional_group.add_argument("--genome", dest="ncbi_build", metavar="{GRCh37,GRCh38,GRCm37,GRCm38}", 
                                type=implemented_genome, help="NCBI build")
    # REQUIRED ONCE
    optional_group.add_argument("--install-genome", dest="install_genome", action='store_true',
                                help="install the given genome for matrix generation (only required on first run "
                                     "per genome)")
    # OPTIONAL
    optional_group.add_argument("--tmp-dir", dest="tmp_dir", metavar="./path/", default="./tmp",
                                type=lambda x: existing_file(x, create_dir=True, shouldnt_exist=True),
                                help="temporary directory for files (default: ./tmp)")
    optional_group.add_argument("--seed", metavar="N", type=int,
                                help="seed for reproducibility")
    optional_group.add_argument("-n", "--nperm", metavar="N", type=int, default=1000,
                                help="number of permutations for p-value estimation (default: 1000)")
    optional_group.add_argument("--alt-allele", dest="alt_allele", choices=["Tumor_Seq_Allele1", "Tumor_Seq_Allele2"],
                                help="Manually set alternative allele")
    optional_group.add_argument("--annotate", metavar="annotate.csv", type=existing_file,  # Location relative to repo
                                default=osp.normpath(osp.join(osp.dirname(__file__), 
                                                     "../resources/mutational_signatures_annotation.csv")),
                                help="Path to signature annotation file (default: "
                                     "../resources/mutational_signatures_annotation.csv)")
    args = parser.parse_args()
    # Check some conditionally required arguments
    arg_conditionals(args)
    return args


def existing_file(path, create_dir=False, shouldnt_exist=False):
    """Check if file or directory exists
    
    :param path: file or directory path 
    :type path: str
    :param create_dir: create directory if non-existent
    :param shouldnt_exist: raise error if exists
    :return: file or directory path
    :rtype: str
    """
    if not os.path.exists(path):
        if not create_dir:
            raise argparse.ArgumentTypeError(f"{path} does not exist.")
        os.mkdir(path)
        return path
    if shouldnt_exist:
        raise argparse.ArgumentTypeError(f"{path} should not exist before running the script.")
    return path


def implemented_genome(genome: str) -> str:
    """Check if genome has been implemented. Ambiguous options default to human genome.
    
    :param genome: genome
    :return: NCBI build
    """
    ncbi_build_options = {
        "19": "GRCh37", "hg19": "GRCh37", "grch37": "GRCh37", "37": "GRCh37", "GRCh37": "GRCh37",
        "grch38": "GRCh38", "38": "GRCh38", "GRCh38": "GRCh38",
        "mm9": "GRCm37", "9": "GRCm37", "grcm37": "GRCm37",
        "mm10": "GRCm38", "10": "GRCm38", "grcm38": "GRCm38"
    }
    ncbi_build = ncbi_build_options.get(genome)
    if ncbi_build is None:
        raise argparse.ArgumentTypeError(f"Genome: `{genome}` has not been implemented. "
                                         f"The following genomes are implemented: {set(ncbi_build_options.values())}")
    return ncbi_build
        
        
def arg_conditionals(args: argparse.Namespace):
    """Check if argument conditionals are followed, else raise error
    
    :param args: argparse parsed arguments
    """
    # If using maf/vcf-folder, study-ID is required
    if (args.maf or args.vcf_folder) and not args.study_id:
        used_command = f"{'--maf' if args.maf else '--vcf-folder'}"
        parameters = f"{'--study-id parameter is' if args.maf else '--study-id and --genome arguments are'}"
        raise argparse.ArgumentTypeError(f"If using the {used_command} command, the {parameters} required")
    # If using vcf-folder, genome is required
    if args.vcf_folder and not args.ncbi_build:
        raise argparse.ArgumentTypeError(f"If using the --vcf-folder argument, the --study-id and "
                                         f"--genome arguments are required")
    # Check if at least one of the signature files is given
    if not (args.sbs_file or args.dbs_file or args.id_file):
        raise argparse.ArgumentTypeError("At least one of the [-s,-d,-i] arguments is required.")


def load_yaml(file: iter) -> dict:
    """Simple load a yaml file. Only accepts key: value pairs, no multi-line statements
    
    :param file: open yaml file 
    :return: yaml key-value pairs
    """
    yaml_cont = {}
    for line in file:
        key, val = line.strip().split(":", 1)
        yaml_cont[key.strip()] = val.strip()
    return yaml_cont


def read_mutations_meta_file(study_path: str) -> dict:
    """Find and read mutation data meta file in cBioPortal study folder
    
    :param study_path: path to cBioPortal study folder
    :return: contents of mutations meta file
    """
    meta_files = glob.glob(osp.join(study_path, "meta*"))
    for file in meta_files:
        with open(file) as f:
            yaml_cont = load_yaml(f)
        if yaml_cont.get("stable_id") == "mutations" and yaml_cont.get("datatype") == "MAF":
            # Correct file, retrieve associated datafile
            print(f"Mutation meta file found in study: \'{file}\'")
            return yaml_cont
    raise FileNotFoundError(f"Mutation meta file not found in study \'{study_path}\'.")


def acquire_maf_study_id(study_path: str) -> tuple:
    """Find meta file in study and retrieve mutations file and study ID
    
    :param study_path: path to cBioPortal study
    :returns: mutation file path, study ID  
    """
    # Find meta files in study
    yaml_cont = read_mutations_meta_file(study_path)
    mut_file = yaml_cont.get("data_filename")
    study_id = yaml_cont.get("cancer_study_identifier")
    mut_file = osp.join(study_path, mut_file)
    return mut_file, study_id


def get_ncbi_build(maf_df: pd.DataFrame) -> str:
    """Try to get the ncbi build from a MAF file
    
    :param maf_df: dataframe containing 'NCBI_Build' column
    :return: ncbi build
    """
    genome = list(maf_df["NCBI_Build"].unique())
    if len(genome) != 1:
        raise ValueError(f"There are either no, or multiple ncbi builds present in your MAF file: \'{genome}\'")
    return implemented_genome(genome[0].lower())


def determine_alternative_allele(maf: pd.DataFrame) -> str:
    """Determine which alternative allele to use from MAF file
    
    :param maf: maf dataframe
    :return: alternative allele to use 
    """
    regexp = re.compile(r'^[ATGC]*$')
    # This is normally advised against due to speed, but we need only one good row.
    for idx, row in maf.iterrows():  
        if row["Tumor_Seq_Allele1"] != row["Reference_Allele"] \
            and regexp.fullmatch(row["Tumor_Seq_Allele1"]):
            return "Tumor_Seq_Allele1"
        elif row["Tumor_Seq_Allele2"] != row["Reference_Allele"] \
            and regexp.fullmatch(row["Tumor_Seq_Allele2"]):
            return "Tumor_Seq_Allele2"
    raise ValueError("Alternative Allele in maf file could not be determined. "
                     "Please use the --alt-allele parameter.")


def preprocess_maf(mut_file: str, tmp_dir: str, args: argparse.Namespace) -> tuple:
    """Preprocess the cBioPortal mutations data file for matrix generation 
    
    :param mut_file: path to mutations file
    :param tmp_dir: path to temp folder
    :param args: parsed argparse.ArgumentParser
    :return: path to preprocessed MAF, NCBI build
    """
    maf = pd.read_csv(mut_file, sep="\t", comment="#", dtype=str)
    # Get NCBI build from MAF file
    if not args.ncbi_build:
        ncbi_build = get_ncbi_build(maf)
    else:
        ncbi_build = args.ncbi_build
    print(f"Using ncbi build: {ncbi_build}.")
    # Determine alt allele
    alt_al = args.alt_allele
    if not alt_al:
        alt_al = determine_alternative_allele(maf)
    print(f"Using '{alt_al}' as alternative allele.")
    # Remap maf file using important parts, as cbioportal MAF format does not conform to
    #  GFC MAF file format (column "Consequence" is not placed on the correct location).
    headers = ["Chromosome", "Start_Position", "End_Position", "Reference_Allele", alt_al, "Tumor_Sample_Barcode"]
    maf = maf[headers]
    new_headers = ["NA" for i in range(16)]
    # SigProfilerMatrixGenerator selects alternative allele by location, so the header doesn't matter
    for i, j in enumerate([4, 5, 6, 10, 12, 15]):  
        new_headers[j] = headers[i]
    maf = maf.reindex(new_headers, axis=1)
    # File needs the MAF file extension, otherwise it is not recognized by SigProfilerMatrixGenerator
    outfile = osp.join(tmp_dir, "mutations.maf")
    # Fill empty cells with NA string, otherwise they get parsed incorrectly by SigProfilerMatrixGenerator
    maf.to_csv(outfile, sep="\t", index=False, na_rep="NA")
    return outfile, ncbi_build


def preprocess_vcf(args: argparse.Namespace, tmp_dir):
    """Preprocess vcf files by copying them into the tmp dir
    
    :param args: argparse parsed arguments
    :param tmp_dir: temporary directory
    """
    vcf_list = glob.glob(osp.join(args.vcf_folder, "*.vcf"))
    for vcf in vcf_list:
        shutil.copy(vcf, osp.join(tmp_dir, osp.basename(vcf)))


def prepare_input(args: argparse.Namespace) -> tuple:
    """Wrapper function for preparing input files for matrix generation
    
    :param args: argparse parsed arguments 
    :return: ncbi_build, study ID
    """
    # Init variables
    maf_file, study_id, ncbi_build = [""] * 3
    
    if args.study_path:
        # Get maf file and study ID from cBioPortal study metadata
        maf_file, study_id = acquire_maf_study_id(args.study_path)
    elif args.maf:
        maf_file = args.maf

    if maf_file:
        # Check if given maf file exists in study path, fix order of columns and copy to temporary directory
        maf_file, ncbi_build = preprocess_maf(maf_file, args.tmp_dir, args)
    else:
        # No MAF, so VCF as input
        preprocess_vcf(args, args.tmp_dir)
    
    # Set ncbi_build and study_id if provided as argument
    if args.ncbi_build:
        ncbi_build = args.ncbi_build
    if args.study_id:
        study_id = args.study_id
    
    return ncbi_build, study_id
    

def genome_install(ncbi_build: str):
    """Install a genome for SigProfilerMatrixGenerator
    
    :param ncbi_build: ncbi build
    """
    print(f"Installing genome: {ncbi_build} for SigProfilerMatrixGenerator.")
    genInstall.install(ncbi_build)


def run_matrix_generator(ncbi_build: str, input_folder: str):
    """Run SigProfilerMatrixGenerator to generate substitution matrices in input folder
    
    :param ncbi_build: ncbi build
    :param input_folder: path to folder containing maf/vcf input files
    """
    print("-" * 120 + "\nGenerating nucleotide matrices: ")
    # Name of the project is irrelevant, the name will not be used any further
    project = "tmp"
    matGen.SigProfilerMatrixGeneratorFunc(project, ncbi_build, input_folder)


def matrix_stable_id(string: str) -> str:
    """Replace non-stableID characters by - and _ in matrix names
    
    :param string: matrix name
    :return: stable ID with only allowed characters
    """
    replace = {
        ">": "-", ":": "_", "[": "_", "]": "_"
    }
    new = ["mutational_signatures_matrix_"]
    for char in string:
        if replace.get(char):
            new.append(replace.get(char))
        else:
            new.append(char)
    return "".join(new)


def matrix_to_cbioportal(matrix: str, outfile: str):
    """Convert a matrix to cBioPortal format
    
    :param matrix: path to input matrix
    :param outfile: path to output file    
    """
    data = pd.read_csv(matrix, sep="\t", dtype=str)
    data = data.rename({"MutationType": "NAME"}, axis=1)
    data = data.set_index("NAME")
    data = data.reset_index()
    data["ENTITY_STABLE_ID"] = data["NAME"].apply(matrix_stable_id) 
    column_order = ["ENTITY_STABLE_ID", "NAME"] + [x for x in data.columns if x not in ["ENTITY_STABLE_ID", "NAME"]]
    data = data[column_order]
    data.to_csv(outfile, sep="\t", index=False, na_rep="NA")    


def preprocess_temposig(file: str):
    """Remove the first value on the first row of a matrix, tempoSig requires this

    :param file: path to matrix file
    """
    with open(file, "r") as infile:
        data = infile.readlines()
    data[0] = "\t".join(data[0].split("\t")[1:])
    with open(file, "w") as outfile:
        outfile.writelines(data)


def run_temposig(temposig_loc: str, inf_matrix: str, out_cont: str, out_pval: str, signature_file: str, nperm=1000, 
                 seed: int=None):
    """Generate mutational signatures using tempoSig
    
    :param temposig_loc: path to tempoSig.R script
    :param inf_matrix: path to input mutational matrix
    :param out_cont: path to output file for contributions
    :param out_pval: path to output file for p-values
    :param signature_file: location of signature file
    :param nperm: number of permutations for p-value estimation (default: 1000)
    :param seed: random number seed for algorithm (default: None)
    """
    command = f"{temposig_loc} {inf_matrix} {out_cont} " \
              f"--pvalue --pv.out {out_pval} --nperm {nperm}  --sigfile {signature_file}"
    if seed:
        command += f" --seed {seed}"
    subprocess.run(command, shell=True)


def temposig_to_generic_assay(infile: str, outfile:str, annotation_file: str, data_type: str) -> str:
    """Transform tempoSig output to the cBioPortal generic assay format
    
    :param infile: path to input file
    :param outfile: path to output file
    :param annotation_file: path to annotation file
    :param data_type: data type used to generate entity stable IDs
    :return: path to output file
    """
    # read in TS output
    data = pd.read_csv(infile, sep="\t", dtype=str, comment="#")
    # Number of mutations column in unnecessary
    data = data.drop("Number of Mutations", axis=1)
    data = data.fillna("NA")
    data.loc[:, data.columns != "Sample Name"] = data.loc[:, 
                                                 data.columns != "Sample Name"].applymap(lambda x: x.upper())
    data = data.set_index("Sample Name")
    # transpose
    data = data.transpose()
    # add extra data
    extra = pd.read_csv(annotation_file, dtype=str)
    extra = extra.set_index("SIGNATURE")
    data = extra.join(data, how='right')
    data = data.reset_index()
    # Generate stable IDs
    data["ENTITY_STABLE_ID"] = data["index"].apply(lambda x: f"mutational_signature_{data_type}_{x}")
    # Output signatures that were not in the annotation file
    sigs = list(data.loc[data['NAME'].isnull(), 'index'])
    if sigs:
        print(f"The following signatures do not have additional annotation: {sigs}")
        data.loc[data.index.isin(sigs), "NAME"] = data.loc[data.index.isin(sigs), "index"]
    data = data.drop("index", axis=1)
    data = data.set_index("ENTITY_STABLE_ID")
    data.to_csv(outfile, sep="\t", na_rep="NA")
    return outfile


def calculate_mutational_signatures(tmp_dir: str, out_dir: str, args: argparse.Namespace) -> list:
    """Wrapper function for mutational signatures algorithms and generating meta file
    
    :param tmp_dir: path to tmp directory
    :param out_dir: path to output directory
    :param args: argparse parsed arguments
    :return: types of matrices for which mutational signatures were created
    """
    matrices_ran = []
    
    print("-" * 120)
    # These substitution matrix filenames are consistent
    for substitution_matrix, matrix_type, cosmic_file in [("output/SBS/tmp.SBS96.all", "SBS", args.sbs_file),
                                                          ("output/DBS/tmp.DBS78.all", "DBS", args.dbs_file),
                                                          ("output/ID/tmp.ID83.all", "ID", args.id_file)]:
        inf_matrix = f"{tmp_dir}/{substitution_matrix}"
        
        # Cannot run without signature file
        if cosmic_file is None:
            print(f"No cosmic signatures file given for matrix type '{matrix_type}'. Skipping...\n")
        # Make sure the substitution matrix was created
        elif not os.path.exists(inf_matrix):
            print(f"Matrix type \'{matrix_type}\' was not created. Skipping...\n")
        else:
            base_cont, base_pval, base_mat = [f"data_mutational_signature_{value}_{matrix_type}.txt" 
                                             for value in ["contribution", "pvalue", "matrix"]]
            
            # Preprocess mutation matrix
            matrix_to_cbioportal(inf_matrix, osp.join(out_dir, base_mat))
            preprocess_temposig(inf_matrix)
            # Preprocess signature file 
            tmp_cosmic = osp.join(tmp_dir, "signature_files", osp.basename(cosmic_file))
            if not osp.exists(osp.join(tmp_dir, "signature_files")):
                os.mkdir(osp.join(tmp_dir, "signature_files"))
            shutil.copy(cosmic_file, tmp_cosmic)
            preprocess_temposig(tmp_cosmic)

            print(f"Running tempoSig for {matrix_type}...")
            run_temposig(args.temposig_loc, inf_matrix, osp.join(tmp_dir, base_cont), osp.join(tmp_dir, base_pval),
                         nperm=args.nperm, seed=args.seed, signature_file=tmp_cosmic)
            if not osp.exists(osp.join(tmp_dir, base_cont)) or not osp.exists(osp.join(tmp_dir, base_pval)):
                print(f"!! Something went wrong with extracting {matrix_type} signatures. File not created.")
            else:
                # Rewrite tempoSig output to cBioPortal format
                temposig_to_generic_assay(osp.join(tmp_dir, base_cont), osp.join(out_dir, base_cont), 
                                          args.annotate, "contribution")
                temposig_to_generic_assay(osp.join(tmp_dir, base_pval), osp.join(out_dir, base_pval),
                                          args.annotate, "pvalue")
                matrices_ran.append(matrix_type)
    print(f"Finished running tempoSig")
    return matrices_ran


def generate_single_meta_file(study_id: str, meta_file: str, file_type: str, algorithm_string: str, out_dir: str, 
                              meta_properties: str="NAME,DESCRIPTION,URL"):
    """Generate a single cBioPortal generic essay meta file
    
    :param study_id: study ID
    :param meta_file: output filename excl. 'meta_' and filetype extension 
    :param file_type: `contribution`, `pvalue` or other value
    :param algorithm_string: string describing the algorithm used
    :param out_dir: path to output directory
    :param meta_properties: generic_entity_meta_properties string (default: 'NAME,DESCRIPTION,URL')
    """
    profile_name = " ".join(meta_file.split("_"))
    out_lines = f"cancer_study_identifier: {study_id}\n" \
                f"genetic_alteration_type: GENERIC_ASSAY\n" \
                f"generic_assay_type: MUTATIONAL_SIGNATURE\n" \
                f"datatype: LIMIT-VALUE\n" \
                f"stable_id: {meta_file}\n" \
                f"profile_name: {profile_name}\n" \
                f"profile_description: profile for {file_type} value of mutational signatures, generated using " \
                f"{algorithm_string}\n" \
                f"data_filename: data_{meta_file}.txt\n" \
                f"show_profile_in_analysis_tab: {str(file_type == 'contribution').lower()}\n" \
                f"generic_entity_meta_properties: {meta_properties}"
    with open(f"{out_dir}/meta_{meta_file}.txt", "w+") as f:
        f.write(out_lines)


def generate_all_meta_files(args: argparse.Namespace, matrices_ran: list, study_id: str, algorithm_string: str):
    """Generate meta files for all files in list
    
    :param args: argparse arguments
    :param matrices_ran: files to generate meta files for
    :param study_id: study ID
    :param algorithm_string: string describing the algorithm used
    """
    for file_type in ["contribution", "pvalue", "matrix"]:
        for matrix in matrices_ran:
            meta_filename = f"mutational_signature_{file_type}_{matrix}"
            print(f"Generating meta file: {args.out_dir}/meta_{meta_filename}.txt...")
            if file_type == "matrix":
                generate_single_meta_file(study_id, meta_filename, file_type, algorithm_string, args.out_dir, 
                                          meta_properties="NAME")
            else:
                generate_single_meta_file(study_id, meta_filename, file_type, algorithm_string, args.out_dir)


def main():
    """Main function"""
    # Parse arguments
    args = parse_args()
    # Prepare input files
    ncbi_build, study_id = prepare_input(args)
    # Install genome if asked
    if args.install_genome:
        genome_install(ncbi_build)
    # Create matrix
    run_matrix_generator(ncbi_build, args.tmp_dir)
    # Run desired algorithm (and possible preprocessing) for mutational signatures
    matrices_ran = calculate_mutational_signatures(args.tmp_dir, args.out_dir, args)
    # Create associated meta files
    generate_all_meta_files(args, matrices_ran, study_id, "tempoSig")


if __name__ == "__main__":
    main()
