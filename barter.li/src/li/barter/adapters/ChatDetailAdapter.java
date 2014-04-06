/*******************************************************************************
 * Copyright 2014, barter.li
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package li.barter.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import li.barter.R;
import li.barter.data.DatabaseColumns;
import li.barter.utils.AppConstants.UserInfo;

/**
 * Class to display Chat messages
 * 
 * @author Vinay S Shenoy
 */
public class ChatDetailAdapter extends CursorAdapter {

    /*
     * View Types. If there are n types of views, these HAVE to be numbered from
     * 0 to n-1
     */
    private static final int      INCOMING_MESSAGE = 0;
    private static final int      OUTGOING_MESSAGE = 1;
    /**
     * Map to maintain a reference between the positions and the view types
     */
    private Map<Integer, Integer> mPositionViewTypeMap;

    public ChatDetailAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        mPositionViewTypeMap = new HashMap<Integer, Integer>();
        buildMapForCursor(cursor);
    }

    /**
     * Traverses the cursor once to make a map of the position to the view type
     * for in memory lookup. Call this whenever the cursor backing the adapter
     * changes
     * 
     * @param cursor The cursor to traverse
     */
    private void buildMapForCursor(Cursor cursor) {

        mPositionViewTypeMap.clear();
        if (cursor != null && !cursor.isClosed()) {
            cursor.moveToPosition(-1);
            String receiverId = null;
            while (cursor.moveToNext()) {
                receiverId = cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.RECEIVER_ID));

                if (receiverId.equals(UserInfo.INSTANCE.getId())) {
                    //Incoming message 
                    mPositionViewTypeMap
                                    .put(cursor.getPosition(), INCOMING_MESSAGE);
                } else {
                    //Outgoing message
                    mPositionViewTypeMap
                                    .put(cursor.getPosition(), OUTGOING_MESSAGE);
                }
            }
        }
    }

    @Override
    public void notifyDataSetChanged() {
        buildMapForCursor(mCursor);
        super.notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {

        return mPositionViewTypeMap.get(position);
    }

    @Override
    public int getViewTypeCount() {
        //Incoming and outgoing message
        return 2;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ((TextView) view.getTag(R.id.text_chat_message)).setText(cursor.getString(cursor.getColumnIndex(DatabaseColumns.MESSAGE)));
        
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        final int viewType = mPositionViewTypeMap.get(cursor.getPosition());
        View view = null;
        if (viewType == INCOMING_MESSAGE) {
            view = LayoutInflater
                            .from(context)
                            .inflate(R.layout.layout_incoming_chat, parent, false);
        } else if (viewType == OUTGOING_MESSAGE) {
            view = LayoutInflater
                            .from(context)
                            .inflate(R.layout.layout_outgoing_chat, parent, false);
        }

        view.setTag(R.id.image_user, view.findViewById(R.id.image_user));
        view.setTag(R.id.text_chat_message, view
                        .findViewById(R.id.text_chat_message));
        return view;
    }

}