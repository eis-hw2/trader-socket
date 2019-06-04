package com.example.tradersocket.Domain.Factory;


import com.example.tradersocket.Domain.Wrapper.SessionWrapper;

import javax.websocket.Session;

public class SessionWrapperFactory {
    public static SessionWrapper create(Session session, String sid){
        return new SessionWrapper(session, sid);
    }
}
