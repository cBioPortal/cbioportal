<?php include("status.php"); ?>
<html>
<head>
<link href="css/style.css" type="text/css" rel="stylesheet" />
<link href="css/jquery.tweet.css" type="text/css" rel="stylesheet" />
<script type="text/javascript" src="js/jquery.min.js"></script>
<script type="text/javascript" src="js/jquery.tweet.js" charset="utf-8"></script>
</head>

<script type='text/javascript'>
    jQuery(function($){
        $(".tweet").tweet({
            username: ["EdwardWinstead", "ecerami"],
            avatar_size: 50,
            count: 3,
            loading_text: "loading tweets..."
        });
    });
</script>
<body>

<h1>cBio Portal</h1>

<table cellpadding=20 width=100%>
<tr valign=bottom>
<td valign=bottom>
<div class="<?php echo($portal_build_status); ?>"></div>
</td>
<td valign=bottom>
<div class="bugs">
<span class="bugs_num"><?php echo($portal_num_bugs); ?></span>
<span class="bugs_text">open bugs</span>
</div>
</td>
<td width=50%>
<div class="tweet"></div> 	
</td>
</tr>
</table>

<?php echo($portal_latest_code); ?>

</body>
</html>
