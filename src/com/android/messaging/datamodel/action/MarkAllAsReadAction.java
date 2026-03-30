/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.messaging.datamodel.action;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseHelper.ConversationColumns;
import com.android.messaging.datamodel.DatabaseWrapper;

public class MarkAllAsReadAction extends Action implements Parcelable {

    public static void markAllAsRead() {
        final MarkAllAsReadAction action = new MarkAllAsReadAction();
        action.start();
    }

    private MarkAllAsReadAction() {
        super();
    }

    @Override
    protected Object executeAction() {
        final DatabaseWrapper db = DataModel.get().getDatabase();

        // Query for all conversations where the latest message is newer than the last read time
        final Cursor cursor = db.query("conversations",
                new String[] { ConversationColumns._ID },
                ConversationColumns.SORT_TIMESTAMP + " > " + ConversationColumns.LAST_READ_TIMESTAMP,
                null, null, null, null);

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    final String conversationId = cursor.getString(0);
                    // Leverage the existing MarkAsReadAction for each conversation.
                    // This securely handles updating the local DB, the Telephony DB,
                    // and clearing system notifications.
                    MarkAsReadAction.markAsRead(conversationId);
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    private MarkAllAsReadAction(final Parcel in) {
        super(in);
    }

    public static final Parcelable.Creator<MarkAllAsReadAction> CREATOR
            = new Parcelable.Creator<MarkAllAsReadAction>() {
        @Override
        public MarkAllAsReadAction createFromParcel(final Parcel in) {
            return new MarkAllAsReadAction(in);
        }

        @Override
        public MarkAllAsReadAction[] newArray(final int size) {
            return new MarkAllAsReadAction[size];
        }
    };

    @Override
    public void writeToParcel(final Parcel parcel, final int flags) {
        writeActionToParcel(parcel, flags);
    }
}
