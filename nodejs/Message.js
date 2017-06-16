 //var table = module.exports = require('azure-mobile-apps').table();
 
 var azureMobileApps = require('azure-mobile-apps'),
 promises = require('azure-mobile-apps/src/utilities/promises'),
 logger = require('azure-mobile-apps/src/logger');

 var table = azureMobileApps.table();

 table.insert(function (context) {
 // For more information about the Notification Hubs JavaScript SDK,
 // see http://aka.ms/nodejshubs
 logger.info('Running TodoItem.insert');

 // Define the GCM payload.
 var payload = {
     "data": {
         "sender": context.item.sender,
         "recipient": context.item.recipient,
         "text": context.item.text
     }
 };   

 // Execute the insert.  The insert returns the results as a Promise,
 // Do the push as a post-execute action within the promise flow.
 return context.execute()
     .then(function (results) {
         // Only do the push if configured
         if (context.push) {
             // Send a GCM native notification.
             context.push.gcm.send(null, payload, function (error) {
                 if (error) {
                     logger.error('Error while sending push notification: ', error);
                 } else {
                     logger.info('Push notification sent successfully!');
                 }
             });
         }
         // Don't forget to return the results from the context.execute()
         return results;
     })
     .catch(function (error) {
         logger.error('Error while running context.execute: ', error);
     });
 });

 module.exports = table;