CalendarQuery myQuery = new CalendarQuery(feedUrl); 
 
DateFormat dfGoogle = new SimpleDateFormat("yyyy-MM-dd'T00:00:00'"); 
Date dt = Calendar.getInstance().getTime(); 
 
myQuery.setMinimumStartTime(DateTime.parseDateTime(dfGoogle.format(dt))); 
// Make the end time far into the future so we delete everything 
myQuery.setMaximumStartTime(DateTime.parseDateTime("2099-12-31T23:59:59")); 
 
// Execute the query and get the response 
CalendarEventFeed resultFeed = service.query(myQuery, CalendarEventFeed.class); 
 
// !!! This returns 25 (or less if there are fewer than 25 entries on the calendar) !!! 
int test = resultFeed.getEntries().size(); 
 
// Delete all the entries returned by the query 
for (int j = 0; j < resultFeed.getEntries().size(); j++) { 
   CalendarEventEntry entry = resultFeed.getEntries().get(j); 
 
   entry.delete(); 
}