package org.nilriri.webbibles.dao;

import android.provider.BaseColumns;

/**
 * Convenience definitions for NotePadProvider
 */
public final class Constants {
    public static final String DATABASE_NAME = "bibles.sqlite";
    public static final int DATABASE_VERSION = 14;

    public static final String EXTERNAL_DB_NAME = "/sdcard/bibles.sqlite";
    public static final int EXTERNAL_DB_VERSION = 14;

    public static final int DATAMANAGE_BACKUP = 0;
    public static final int DATAMANAGE_RESTORE = 1;
    public static final int DATAMANAGE_DELINTERNAL = 2;
    public static final int DATAMANAGE_DELEXTERNAL = 3;
    public static final int DATAMANAGE_DOWNLOAD = 4;

    public static final String DEFAULT_SORT_ORDER = "_id DESC";

    // This class cannot be instantiated
    private Constants() {
    }

    /**
     * Bibles table
     */
    public static final class Bibles implements BaseColumns {
        // This class cannot be instantiated
        private Bibles() {
        }

        public static final String[] VERSION_TABLE_NAME = new String[] { "GAE", "HAN", "SAE", "SAENEW", "COG", "COGNEW", "CEV", "C3TV6", "C3TV7", "C3TV8", "C3TV9", "C3TV10", "C3TV11", "C3TV12", "C3TV13", "C3TV14", "C3TV15", "C3TV16", "C3TV17" };

        public static final String _ID = "_id";
        public static final String VERCODE = "vercode";
        public static final String VERNAME = "vername";
        public static final String VERSION = "version";
        public static final String BOOK = "book";
        public static final String CHAPTER = "chapter";
        public static final String VERSE = "verse";
        public static final String CONTENTS = "contents";
        public static final String STYLE = "style";

        public static final int COL_ID = 0;
        public static final int COL_VERCODE = 1;
        public static final int COL_VERNAME = 2;
        public static final int COL_VERSION = 3;
        public static final int COL_BOOK = 4;
        public static final int COL_CHAPTER = 5;
        public static final int COL_VERSE = 6;
        public static final int COL_CONTENTS = 7;
        public static final int COL_STYLE = 8;

    }

    public static final class Notes implements BaseColumns {
        // This class cannot be instantiated
        private Notes() {
        }

        public static final String NOTE_TABLE_NAME = "biblenote";

        public static final String _ID = "_id";
        public static final String BIBLEID = "bibleid";
        public static final String VERSION = "version";
        public static final String BOOK = "book";
        public static final String CHAPTER = "chapter";
        public static final String VERSE = "verse";
        public static final String VERSESTR = "versestr";
        public static final String TITLE = "title";
        public static final String CONTENTS = "contents";
        public static final String MODIFIED_DATE = "writedate";
        public static final String SCORE = "score";

        public static final int COL_ID = 0;
        public static final int COL_BIBLEID = 1;
        public static final int COL_VERSION = 2;
        public static final int COL_BOOK = 3;
        public static final int COL_CHAPTER = 4;
        public static final int COL_VERSE = 5;
        public static final int COL_VERSESTR = 6;
        public static final int COL_TITLE = 7;
        public static final int COL_CONTENTS = 8;
        public static final int COL_MODIFIED_DATE = 9;
        public static final int COL_SCORE = 10;

    }

    public static final class Bookmark implements BaseColumns {
        // This class cannot be instantiated
        private Bookmark() {
        }

        public static final String BOOKMARK_TABLE_NAME = "bookmark";

        public static final String _ID = "_id";
        public static final String VERCODE = "vercode";
        public static final String BIBLEID = "bibleid";
        public static final String VERSION = "version";
        public static final String BOOK = "book";
        public static final String CHAPTER = "chapter";
        public static final String VERSE = "verse";
        public static final String MODIFIED_DATE = "writedate";
        public static final String VERSESTR = "versestr";

        public static final int COL_ID = 0;
        public static final int COL_VERCODE = 1;
        public static final int COL_BIBLEID = 2;
        public static final int COL_VERSION = 3;
        public static final int COL_BOOK = 4;
        public static final int COL_CHAPTER = 5;
        public static final int COL_VERSE = 6;
        public static final int COL_MODIFIED_DATE = 7;
        public static final int COL_VERSESTR = 8;

    }

    public static final class Favorites implements BaseColumns {
        private Favorites() {
        }

        public static final String FAVORITES_TABLE_NAME = "favorites";

        public static final String _ID = "_id";
        public static final String BIBLEID = "bibleid";
        public static final String GROUPKEY = "groupkey";
        public static final String VERSESTR = "versestr";
        public static final String VERSION = "version";
        public static final String BOOK = "book";
        public static final String CHAPTER = "chapter";
        public static final String VERSE = "verse";
        public static final String CONTENTS = "contents";

        public static final String GROUPNM = "groupnm";

        public static final int COL_ID = 0;
        public static final int COL_BIBLEID = 1;
        public static final int COL_GROUPKEY = 2;
        public static final int COL_VERSESTR = 3;
        public static final int COL_VERSION = 4;
        public static final int COL_BOOK = 5;
        public static final int COL_CHAPTER = 6;
        public static final int COL_VERSE = 7;
        public static final int COL_CONTENTS = 8;

    }

    public static final class FavoriteGroup implements BaseColumns {
        private FavoriteGroup() {
        }

        public static final String FAVORITEGROUP_TABLE_NAME = "favoritegroup";

        public static final String _ID = "_id";
        public static final String GROUPNM = "groupnm";

        public static final int COL_ID = 0;
        public static final int COL_GROUPNM = 1;

    }

    public static final class Songs implements BaseColumns {
        private Songs() {
        }

        public static final String SONGS_TABLE_NAME = "songs";

        public static final String _ID = "_id";
        public static final String VERSION = "version";
        public static final String SONGID = "songid";
        public static final String SONGTEXT = "songtext";
        public static final String SUBJECT = "subject";
        public static final String TITLE = "title";
        public static final String IMGURL = "imgurl";

        public static final int COL_ID = 0;
        public static final int COL_VERSION = 1;
        public static final int COL_SONGID = 2;
        public static final int COL_SONGTEXT = 3;
        public static final int COL_SUBJECT = 4;
        public static final int COL_TITLE = 5;
        public static final int COL_IMGURL = 6;

    }

    public static final class Schedule implements BaseColumns {
        private Schedule() {
        }

        public static final String SCHEDULE_TABLE_NAME = "schedule";

        public static final String _ID = "_id";
        public static final String TONUMBER = "tonumber";
        public static final String TONAME = "toname";
        public static final String STARTDAY = "startday";
        public static final String ENDDAY = "endday";
        public static final String SENDTIME = "sendtime";
        public static final String SENDGROUP = "sendgroup";

        public static final int COL_ID = 0;
        public static final int COL_TONUMBER = 1;
        public static final int COL_TONAME = 2;
        public static final int COL_STARTDAY = 3;
        public static final int COL_ENDDAY = 4;
        public static final int COL_SENDTIME = 5;
        public static final int COL_SENDGROUP = 6;

    }

}
