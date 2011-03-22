ContentURI uri = ContentURI.create("geo:" + myContentHandler2.getLatitude()
     + "," + myContentHandler2.getLongitude());
Intent intent = new Intent("android.intent.action.VIEW", uri);
startActivity(intent);
