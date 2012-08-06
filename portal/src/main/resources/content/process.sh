$MARKDOWN -x toc web_api.markdown > ../../webapp/content/web_api.html
$MARKDOWN -x toc cgds_r.markdown > ../../webapp/content/cgds_r.html
$MARKDOWN -x toc news.markdown > ../../webapp/content/news.html
$MARKDOWN -x toc news_tcga.markdown > ../../webapp/content/news_tcga.html
$MARKDOWN -x toc faqs.markdown > ../../webapp/content/faq.html
$MARKDOWN -x toc faqs_tcga.markdown > ../../webapp/content/faq_tcga.html
$MARKDOWN -x toc data_sets.markdown > ../../webapp/content/data_sets.html
$MARKDOWN -x toc data_sets_right_column.markdown > ../../webapp/content/data_sets_right_column.html
$MARKDOWN -x toc data_sets_tcga_right_column.markdown > ../../webapp/content/data_sets_tcga_right_column.html
$MARKDOWN -x toc about_us.markdown > ../../webapp/content/about_us.html
$MARKDOWN -x toc networks.markdown > ../../webapp/content/networks.html
$MARKDOWN -x toc onco_spec_lang_desc.markdown > ../../webapp/content/onco_spec_lang_desc.html
$MARKDOWN -x toc examples.markdown > ../../webapp/content/examples.html
$MARKDOWN -x toc examples_tcga.markdown > ../../webapp/content/examples_tcga.html

$MARKDOWN -x toc news_su2c.markdown > ../../webapp/content/news_su2c.html
$MARKDOWN -x toc faqs_su2c.markdown > ../../webapp/content/faq_su2c.html
$MARKDOWN -x toc data_sets_su2c_right_column.markdown > ../../webapp/content/data_sets_su2c_right_column.html
$MARKDOWN -x toc examples_su2c.markdown > ../../webapp/content/examples_su2c.html
$MARKDOWN -x toc about_us_su2c.markdown > ../../webapp/content/about_us_su2c.html

$MARKDOWN -x toc data_sets_public_right_column.markdown > ../../webapp/content/data_sets_public_right_column.html

cp data_sets_tcga.html ../../webapp/content/
cp data_sets_su2c.html ../../webapp/content/
cp data_sets_public.html ../../webapp/content/
