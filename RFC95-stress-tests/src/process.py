#%%
import pandas as pd
import os

print(os.getcwd())

csv_dir = '../out/resource_usage'
csv_files = [f for f in os.listdir(csv_dir) if f.endswith('.csv')]

resources = {}
runs = {}
for file in csv_files:
    file_path = os.path.join(csv_dir, file)

    key = os.path.splitext(file)[0]
    resources[key] = pd.read_csv(file_path)

    resources[key]['download_id'] = resources[key].apply(
        lambda row: f"{row['PID']}_{row['Study_ID']}", axis=1)
    
    resources[key] = resources[key].groupby('download_id').apply(
        lambda group: group.assign(time=group['Timestamp'] - group['Timestamp'].min())
    ).reset_index(drop=True)

    resources[key]['time'] = resources[key]['time'] / 60  # Convert time to minutes

    key_parts = key.split('-')
    runs[key] = {'count': len(key_parts), 'study_ids': key_parts}


for i in resources.keys(): print(i)

# [[key, value['count']] for key, value in runs.items()]

# % plot download time vs concurrent downloads

# run_keys = [key for key, value in runs.items(
# ) if study_id in key]

run_keys = runs.keys()

time_list = []
for key in run_keys:
    time_list.append([key, runs[key]['study_ids'][0],
                      resources[key]['time'].max(), 
                      resources[key]['download_id'].iloc[0],
                      runs[key]['count']])

times = pd.DataFrame(time_list, columns=['Run', 'study_id', 'Max time (min)', 'Download ID', 'Concurrent Downloads'])
times.sort_values(by='Max time (min)', inplace=True)

times['study_id'] = times['study_id'].replace({'all_phas': 'all_phase2_target_2018_pub'})

import matplotlib.pyplot as plt

plt.figure(figsize=(10, 4))
for study in times['study_id'].unique():
    subset = times[times['study_id'] == study]
    plt.scatter( subset['Max time (min)'], subset['Concurrent Downloads'],label=study)

plt.title(f'Download time with varying concurrent downloads')
plt.ylabel('Concurrent Downloads')
plt.xlabel('Max time (min)')
plt.legend(title='Study ID', loc='upper left', bbox_to_anchor=(1, 1))
plt.tight_layout()
plt.show()


# %%

# plot memory usage vs time for a specific number of concurrent downloads
study_count = 1

run_keys = [key for key, value in runs.items() if value['count'] == study_count]
# run_keys = [key for key in run_keys if 'all_pha' in key]

print(run_keys)
if len(run_keys) == 0:
    raise ValueError(f"No runs found with {study_count} concurrent downloads.")

filtered_resources = pd.concat([resources[key] for key in run_keys if key in resources], ignore_index=True)
# filtered_file_sizes = pd.concat([file_sizes[key] for key in run_keys if key in file_sizes], ignore_index=True)

import matplotlib.pyplot as plt

plt.figure(figsize=(10, 4))

for download_id, group in filtered_resources.groupby('download_id'):
    line, = plt.plot(group['time'], group['Memory_Usage_MB'], label=download_id)
    plt.plot(group['time'].iloc[-1], group['Memory_Usage_MB'].iloc[-1], 'o', markersize=10, color=line.get_color())

plt.ylim(2, 5)
# plt.xlim(-.2, 10.5)
plt.xlabel('Time (min)')
plt.ylabel('Memory Usage (MB)')
plt.title(f'{study_count} concurrent downloads')
plt.legend(loc='upper right', bbox_to_anchor=(1.5, 1), fontsize='small')
plt.tight_layout()
plt.show()

plt.figure(figsize=(10, 4))

for download_id, group in filtered_resources.groupby('download_id'):
    line, = plt.plot(group['time'], group['CPU_Usage'], label=download_id)
    plt.plot(group['time'].iloc[-1], group['CPU_Usage'].iloc[-1], 'o', markersize=10, color=line.get_color())

plt.ylim(-.1, 2)
# plt.xlim(-.2, 10.5)

plt.xlabel('Time (min)')
plt.ylabel('CPU Usage (%)')
plt.title(f'{study_count} concurrent downloads')
plt.legend(loc='upper right', bbox_to_anchor=(1.5, 1), fontsize='small')
plt.tight_layout()
plt.show()


# %%

studies_dir = '../studies'
clinical_files = [os.path.join(studies_dir, study, 'data_clinical_sample.txt') 
                  for study in os.listdir(studies_dir) 
                  if os.path.isdir(os.path.join(studies_dir, study))]

sample_counts = []
for file in clinical_files:
    if os.path.exists(file):
        clinical_data = pd.read_csv(file, sep='\t', comment='#')
        sample_count = clinical_data['SAMPLE_ID'].nunique()
        sample_counts.append({'Study_ID': os.path.basename(os.path.dirname(file)), 'Sample_Count': sample_count})

sample_counts_df = pd.DataFrame(sample_counts)

sizes = sizes.merge(sample_counts_df, on='Study_ID', how='left')
sizes



# %%

