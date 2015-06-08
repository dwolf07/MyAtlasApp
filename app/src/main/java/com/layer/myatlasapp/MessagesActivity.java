package com.layer.myatlasapp;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.layer.atlas.AtlasMessageComposer;
import com.layer.atlas.AtlasMessagesList;
import com.layer.atlas.AtlasParticipantPicker;
import com.layer.atlas.AtlasTypingIndicator;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Message;

import java.util.Set;

/**
 * Created by neilmehta on 5/28/15.
 */
public class MessagesActivity extends ActionBarActivity {

    private AtlasMessagesList messagesList;
    private AtlasParticipantPicker participantPicker;
    private AtlasTypingIndicator typingIndicator;
    private AtlasMessageComposer atlasComposer;
    private Conversation conversation;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages_screen);

        Uri id = getIntent().getParcelableExtra("conversation-id");
        if(id != null)
            conversation = ConversationsActivity.layerClient.getConversation(id);

        messagesList = (AtlasMessagesList) findViewById(R.id.messageslist);
        messagesList.init(ConversationsActivity.layerClient, ConversationsActivity.participantProvider);
        messagesList.setConversation(conversation);

        participantPicker = (AtlasParticipantPicker) findViewById(R.id.participantpicker);
        String[] currentUser = {ConversationsActivity.layerClient.getAuthenticatedUserId()};
        participantPicker.init(currentUser, ConversationsActivity.participantProvider);
        if(conversation != null)
            participantPicker.setVisibility(View.GONE);

        typingIndicator = (AtlasTypingIndicator) findViewById(R.id.typingindicator);
        typingIndicator.init(conversation, new AtlasTypingIndicator.Callback(){
            public void onTypingUpdate(AtlasTypingIndicator indicator, Set<String> typingUserIds) {
            }
        });

        atlasComposer = (AtlasMessageComposer) findViewById(R.id.textinput);
        atlasComposer.init(ConversationsActivity.layerClient, conversation);
        atlasComposer.setListener(new AtlasMessageComposer.Listener(){
            public boolean beforeSend(Message message) {
                if(conversation == null){
                    String[] participants = participantPicker.getSelectedUserIds();
                    if(participants.length > 0){
                        participantPicker.setVisibility(View.GONE);
                        conversation = ConversationsActivity.layerClient.newConversation(participants);
                        messagesList.setConversation(conversation);
                        atlasComposer.setConversation(conversation);
                    } else {
                        return false;
                    }
                }
                return true;
            }
        });
    }

    protected void onResume() {
        super.onResume();
        ConversationsActivity.layerClient.registerEventListener(messagesList);
    }

    protected void onPause(){
        super.onPause();
        ConversationsActivity.layerClient.unregisterEventListener(messagesList);
    }
}
