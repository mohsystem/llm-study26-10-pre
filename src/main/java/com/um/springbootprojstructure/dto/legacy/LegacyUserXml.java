package com.um.springbootprojstructure.dto.legacy;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LegacyUserXml {

    @JacksonXmlProperty(localName = "id")
    private String id;

    @JacksonXmlProperty(localName = "username")
    private String username;

    @JacksonXmlProperty(localName = "email")
    private String email;

    @JacksonXmlProperty(localName = "displayName")
    private String displayName;

    @JacksonXmlProperty(localName = "enabled")
    private Boolean enabled;
}