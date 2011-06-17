$MARKDOWN -x toc web_api.markdown > ../web/content/web_api.html
$MARKDOWN -x toc cgds_r.markdown > ../web/content/cgds_r.html
$MARKDOWN -x toc news.markdown > ../web/content/news.html
$MARKDOWN -x toc news_tcga.markdown > ../web/content/news_tcga.html
$MARKDOWN -x toc faqs.markdown > ../web/content/faq.html
$MARKDOWN -x toc faqs_tcga.markdown > ../web/content/faq_tcga.html
$MARKDOWN -x toc data_sets.markdown > ../web/content/data_sets.html
$MARKDOWN -x toc data_sets_right_column.markdown > ../web/content/data_sets_right_column.html
$MARKDOWN -x toc data_sets_tcga_right_column.markdown > ../web/content/data_sets_tcga_right_column.html
$MARKDOWN -x toc about_us.markdown > ../web/content/about_us.html
$MARKDOWN -x toc networks.markdown > ../web/content/networks.html
$MARKDOWN -x toc onco_spec_lang_desc.markdown > ../web/content/onco_spec_lang_desc.html
$MARKDOWN -x toc examples.markdown > ../web/content/examples.html
$MARKDOWN -x toc examples_tcga.markdown > ../web/content/examples_tcga.html

$MARKDOWN -x toc news_su2c.markdown > ../web/content/news_su2c.html
$MARKDOWN -x toc faqs_su2c.markdown > ../web/content/faq_su2c.html
$MARKDOWN -x toc data_sets_su2c_right_column.markdown > ../web/content/data_sets_su2c_right_column.html
$MARKDOWN -x toc examples_su2c.markdown > ../web/content/examples_su2c.html
$MARKDOWN -x toc about_us_su2c.markdown > ../web/content/about_us_su2c.html

cp data_sets_tcga.html ../web/content/
cp data_sets_su2c.html ../web/content/
