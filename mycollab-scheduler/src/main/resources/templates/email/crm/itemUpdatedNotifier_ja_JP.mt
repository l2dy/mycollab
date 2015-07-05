<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Item is updated</title>
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.3.0/css/font-awesome.min.css">
</head>
<body style="background-color: ${styles.background}; font: ${styles.font}; color: #4e4e4e; padding: 0px 0px;">
	#macro( hyperLink $displayName $webLink )
		<a href="$webLink" style="color: ${styles.link_color}; white-space: normal;">$displayName</a>
	#end
	
    <table width="800" cellpadding="0" cellspacing="0" border="0" style="margin: 20px auto;">
       <tr>
       		<td>
       			<div style="padding: 10px 30px;">
       				<img src="${defaultUrls.cdn_url}icons/logo.png" alt="The power productivity tool for your organization" width="130" height="30"
       				style="margin: 0px; padding: 0px;">
       			</div>
       		</td>			
		</tr>
        <tr>
            <td style="padding: 10px 30px 0px;">
				<p>$actionHeading</p>
				<p>
				#foreach( $title in $titles )
					#if( $foreach.count > 1 )
						<span style="color: rgb(36, 127, 211);">&#9474;</span>
					#end
					#hyperLink( $title.displayName $title.webLink )
				#end
				</p>
				<p><b>
				#hyperLink( $summary $summaryLink )
				</b></p>
                <table width="100%" cellpadding="0" cellspacing="0" border="0" style="font-size: 12px; margin: 0px
                0px 25px;">
                	<tr>
                        <td style="padding: 3px 0px;">
                    		<p><u><i>Changes:</i></u></p>
                    		<table border="0" cellspacing="0" style="font-size: 12px; width:100%; border-width: 1px
                    		1px 0px 0px; border-style: solid; border-color: rgb(211, 239, 253);">
                    			<tr>
                    				<td style="font-weight: bold; border-bottom: 1px solid rgb(169, 169, 169); width:240px; padding: 10px; border-width: 0px 0px 1px 1px; border-style: solid; border-color: rgb(211, 239, 253);">Field</td>
                    				<td style="font-weight: bold; border-bottom: 1px solid rgb(169, 169, 169); width:250px; padding: 10px; border-width: 0px 0px 1px 1px; border-style: solid; border-color: rgb(211, 239, 253);">Old Value</td>
                    				<td style="font-weight: bold; border-bottom: 1px solid rgb(169, 169, 169); width:250px; padding: 10px; border-width: 0px 0px 1px 1px; border-style: solid; border-color: rgb(211, 239, 253);">New Value</td>
                    			</tr>
                    			#foreach ($item in $historyLog.ChangeItems)
                    				#if ($mapper.hasField($item.field))
                    				#set($fieldFormat=$mapper.getFieldLabel($item.field))
                    				<tr>
                    					<td valign="top" style="width:240px; padding: 10px; background-color: rgb(232, 246, 255); border-width: 0px 0px 1px 1px; border-style: solid; border-color: rgb(211, 239, 253);">
                    						$context.getMessage($fieldFormat.displayName)
                    					</td>
                    					<td valign="top" style="width: 250px ;word-wrap: break-word; white-space: normal; word-break: break-all; padding: 10px; border-width: 0px 0px 1px 1px; border-style: solid; border-color: rgb(211, 239, 253);">
                    						$fieldFormat.formatField($context, $item.oldvalue)
                    					</td>
                    					<td valign="top" style="width: 250px ;word-wrap: break-word; white-space: normal; word-break: break-all; padding: 10px; border-width: 0px 0px 1px 1px; border-style: solid; border-color: rgb(211, 239, 253);">
                    						$fieldFormat.formatField($context, $item.newvalue)
                    					</td>
                    				</tr>
                    				#end
                    			#end
                    		</table>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        #parse("templates/email/footer_en_US.mt")
    </table>
</body>
</html>