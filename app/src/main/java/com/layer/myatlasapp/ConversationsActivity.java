package com.layer.myatlasapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.layer.atlas.Atlas;
import com.layer.atlas.AtlasConversationsList;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;

import java.util.HashMap;
import java.util.Map;

public class ConversationsActivity extends ActionBarActivity {

    static public String AppID = "3f098776-0618-11e5-a6b3-84d0e30072a2";

    static public LayerClient layerClient;
    static public Atlas.ParticipantProvider participantProvider;

    private AtlasConversationsList myConversationList;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversations_screen);

        layerClient = LayerClient.newInstance(this, AppID);
        layerClient.registerAuthenticationListener(new MyAuthenticationListener(this));
        layerClient.connect();
        if(!layerClient.isAuthenticated()) {
            layerClient.authenticate();
        } else {
            onUserAuthenticated();
        }
    }

    public static String getUserID(){
        if(Build.FINGERPRINT.startsWith("generic"))
            return "Simulator";
        return "Device";
    }

    static class User implements Atlas.Participant{
        private String name;
        public User(String id) { name = id; }
        public String getFirstName() { return name; }
        public String getLastName() { return ""; }
    }

    public void onUserAuthenticated(){
        participantProvider  = new Atlas.ParticipantProvider() {
            Map<String, Atlas.Participant> users = new HashMap<String, Atlas.Participant>();
            {
                users.put("Device", new User("Device"));
                users.put("Simulator", new User("Simulator"));
                users.put("Dashboard", new User("Web"));
            }
            public Map<String, Atlas.Participant> getParticipants(String filter,
                                                                  Map<String, Atlas.Participant> result) {

                for(Map.Entry<String, Atlas.Participant> entry : users.entrySet()){
                    if(entry.getValue().getFirstName().indexOf(filter) > -1)
                        result.put(entry.getKey(), entry.getValue());
                }

                return result;
            }
            public Atlas.Participant getParticipant(String userId) {
                return users.get(userId);
            }
        };

        myConversationList = (AtlasConversationsList)findViewById(R.id.conversationlist);
        myConversationList.init(layerClient, participantProvider);
        myConversationList.setClickListener(new AtlasConversationsList.ConversationClickListener() {
            public void onItemClick(Conversation conversation) {
                startMessagesActivity(conversation);
            }
        });

        layerClient.registerEventListener(myConversationList);

        View newconversation = findViewById(R.id.newconversation);
        newconversation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startMessagesActivity(null);
            }
        });
    }




    private void startMessagesActivity(Conversation c){
        Intent intent = new Intent(ConversationsActivity.this, MessagesActivity.class);
        if(c != null)
            intent.putExtra("conversation-id",c.getId());
        startActivity(intent);
    }
}
