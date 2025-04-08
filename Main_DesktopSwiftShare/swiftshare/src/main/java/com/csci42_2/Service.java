package com.csci42_2;

import javax.bluetooth.ServiceRecord;

public class Service {

    private ServiceRecord service;
    private String name;
    private String url;

    public Service (ServiceRecord sr) {
        service = sr;
        try {
            name = service.getHostDevice().getFriendlyName(false);
            url = service.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
        } catch (Exception e) {
            e.printStackTrace();
            name = "No Name";
        }
    }

    @Override
    public String toString() {
        return name;
    }

    public String getURL() {
        return url;
    }
    
}
