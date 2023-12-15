Just an FYI on this:  the docs in content will be in markdown format.

I have a script /content/process.sh, that converts the markdown to html and places the results in web/content.

To get the script to work on your machines, you will need to follow the instructions here:

http://www.freewisdom.org/projects/python-markdown/Installation

wget http://pypi.python.org/packages/source/M/Markdown/Markdown-2.0.tar.gz
tar xvzf Markdown-2.0.tar.gz
cd Markdown-2.0/
sudo python setup.py install

Then, add a line like this to your .bash_profile or .bash_login file:

export MARKDOWN=/Users/cerami/libraries/Markdown-2.0/markdown.py

Then, you source .bash_login, e.g.

source .bash_login, and you will be able to run content/process.sh


