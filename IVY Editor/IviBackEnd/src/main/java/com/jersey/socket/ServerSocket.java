package com.jersey.socket;

import com.google.gson.JsonArray;
import com.jersey.utils.JSONUtil;

import java.util.List;
import java.util.Map;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;


@ServerEndpoint("/endpoint")
public class ServerSocket {

    JSONUtil jsonUtil = new JSONUtil();

    @OnOpen
    public void onOpen(Session session) {

        /////////////////////////////////////////////////////////////////////////////
        // Access request parameters from URL query String.
        // If a client subscribes, add Session to PushTimeService.
        //
        Map<String, List<String>> params = session.getRequestParameterMap();


        if (params.get("push") != null && (params.get("push").get(0).equals("REFRESH"))){
            RefreshService.initialize();
            //RefreshService.add(session);
            RefreshService.add2(session);
        }
        /////////////////////////////////////////////////////////////////////////////
    }
    @OnClose
    public void onClose(Session session) {
        String token = RefreshService.getTokenFromSession(session);
        RefreshService.removeFromAllGroups(token);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        String type = null;
        String token = null;
        String groupName = null;
        JsonArray groupsArray;

        jsonUtil.setParams(message);

        type = jsonUtil.parse("type");

        switch (type) {
            case  "register":
                groupName = jsonUtil.parse("group");
                token = jsonUtil.parse("token");
                RefreshService.registerToGroup(token, groupName);
                break;
            case  "unregister":
                groupName = jsonUtil.parse("group");
                token = jsonUtil.parse("token");
                RefreshService.unregisterFromGroup(token, groupName);
                break;
            case "multiregister":
                groupsArray = jsonUtil.parseArray("groups");
                token = jsonUtil.parse("token");
                for(int i = 0; i < groupsArray.size(); i++){
                    RefreshService.registerToGroup(token, groupsArray.get(i).getAsString());
                }
                break;
            case "multiunregister":
                groupsArray = jsonUtil.parseArray("groups");
                token = jsonUtil.parse("token");
                for(int i = 0; i < groupsArray.size(); i++){
                    RefreshService.unregisterFromGroup(token, groupsArray.get(i).getAsString());
                }
                break;
            case "unregisterall":
                token = jsonUtil.parse("token");
                RefreshService.unregisterFromAllGroups(token);
                break;
            case  "notify":
                groupName = jsonUtil.parse("group");
                if(groupName != null && groupName.length() > 0) {
                    RefreshService.sendGroupNotification("refresh-table", groupName);
                }
                break;
            default:
                break;
        }
    }

    @OnError
    public void onError(Throwable t) {
    }
}

