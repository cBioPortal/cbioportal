<!-- Include Global List of Javascript Files to Load -->
<script type="text/javascript" src="js/jquery.min.js"></script>
<script type="text/javascript" src="js/jquery.tipTip.minified.js"></script>
<script type="text/javascript" src="js/jquery.address-1.4.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.8.14.custom.min.js"></script>
<script type="text/javascript" src="js/jquery.cookie.js"></script>
<script type="text/javascript" src="js/cgx_jquery.js"></script>
<script type="text/javascript" src="js/global-tabs.js"></script>
<script type="text/javascript" src="js/jquery.popeye-2.0.4.min.js"></script>
<script type="text/javascript" src="js/mailme.js"></script>
<script type="text/javascript" src="js/jquery.dataTables.min.js"></script>

<!-- Google Analytics -->
<script type="text/javascript">

  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', 'UA-17134933-1']);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();
</script>

<!-- De-obfuscate All Email Addresses -->
<script type="text/javascript">
    <!-- When the document is ready, de-obfuscate the email addresses -->
    $(document).ready(function() {
        $('span.mailme').mailme();
    });
</script>
