## How does the study view organize the charts
Study view page is fully responsive, it will try to fit as many charts as possible based on the browser's width and height.

The layout of a chart is determined mainly based on priority. Higher priority will promote chart closer to the left-top. 

In order to improve the layout, we added a layout algorithm layer. The study view page is using grid layout. All charts will be put into 2-dimensional systems. For example, pie chart, by default, takes 1 block and bar chart uses two blocks. All charts will be placed from left to right, top to bottom. In order to prevent misalignment, we promote small charts to fit into the space. 

For logged-in(authenticated) users, charts layout is saved to users profile i.e, whenever user tries to re-visits the same url, previously saved layout will be loaded.