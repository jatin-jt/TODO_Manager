package com.jatin.todomanager.db;

/**
 * Created by Dell on 13-07-2016.
 */
public class TaskTable extends DbTable {

    public static final String TABLE_NAME = "tasks";

    public interface Columns {
        String ID = "id";
        String TITLE = "title";
        String DATE = "date";
    }

    public static final String TABLE_CREATE_CMD =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                    LBR
                    +Columns.ID + TYPE_INT_PK+" AUTOINCREMENT " + COMMA
                    + Columns.TITLE + TYPE_TEXT + COMMA
                    + Columns.DATE + TYPE_TEXT +
                    RBR + ";";
}