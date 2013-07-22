function cells = urlgetcells(url, verbose)
% this function is only used internally by the CGDS matlab toolbox
% returns a 2D cell array where each cell contains a tab-delimited 'cell'
% from the server output

% decrease if out of memory errors are encountered
nPrealloc = 10000;

S = urlread(url);

rows = textscan(S, '%s', 'delimiter', '\n', 'BufSize', 65535);
rows = rows{1};

cells = cell(nPrealloc, 1);
n = 0;
for i = 1:length(rows)
    thisRow = rows{i};
    if strcmp(thisRow(1), '#')
        if verbose
            fprintf('%s\n', thisRow);
        end
    elseif strcmp(thisRow(1:6), 'Error:')
        fprintf('%s\n', thisRow);
        error('Cgds:getcancertypes:CgdsError','CGDS returned an error.');
    else
        % this row contains data/header rather than status/warnings/errors    
        n = n + 1;
        thisCells = textscan(thisRow, '%s', 'delimiter', '\t', 'BufSize', 65535);
        thisCells = thisCells{1};
        cells(n, 1:length(thisCells)) = thisCells;
    end
end
cells = cells(1:n, :);
