package com.webappfinal.final_webapp.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtility {    
    private CookieUtility() {
        // Private constructor to prevent instantiation
    }

    public static String getCookieValue(HttpServletRequest req, String cookieName) {
        if (req.getCookies() == null) {
            return null;
        } 
        for(Cookie cookies : req.getCookies()) {
            if(cookieName.equals(cookies.getName())) {
                return cookies.getValue();
            }
        }
        return null;
    }
    public static void addCookie(HttpServletResponse res, String name, String value, int maxAge) {
        Cookie c = new Cookie(name, value);
        c.setHttpOnly(true);
        c.setPath("/");
        c.setMaxAge(maxAge);
        res.addCookie(c);
    }
    public static void deleteCookie(HttpServletResponse res, String name) {
        Cookie c = new Cookie(name, "");
        c.setHttpOnly(true);
        c.setPath("/");
        c.setMaxAge(0);
        res.addCookie(c);
    }
}
