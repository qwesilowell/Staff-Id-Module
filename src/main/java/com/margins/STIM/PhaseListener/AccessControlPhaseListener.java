/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.PhaseListener;

import com.margins.STIM.Bean.UserSession;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import java.io.IOException;
import java.io.Serial;

/**
 *
 * @author PhilipManteAsare
 */
public class AccessControlPhaseListener implements PhaseListener {

    @Serial
    private static final long serialVersionUID = 1L;

//    @Override
//    public void beforePhase(PhaseEvent event) {
//        FacesContext context = event.getFacesContext();
//        ExternalContext externalContext = context.getExternalContext();
//        String path = externalContext.getRequestServletPath();
//
//        // Skip login, error, or public pages
//        boolean isPublicPage = path.contains("login.xhtml")
//                || path.contains("access-denied.xhtml")
//                || path.contains("sublogin.xhtml")
//                || path.contains("BiometricLogin.xhtml")
//                || path.contains("public/");
//        
//        if (isPublicPage) {
//            return;
//        }
//
//        UserSession sessionBean = context.getApplication()
//                .evaluateExpressionGet(context, "#{userSession}", UserSession.class);
//
//        if (sessionBean == null || !sessionBean.hasAccessTo(path)) {
//            try {
//                externalContext.redirect(externalContext.getRequestContextPath() + "/unauthorized.xhtml");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
    @Override
    public void afterPhase(PhaseEvent event) {
        FacesContext context = event.getFacesContext();
        ExternalContext externalContext = context.getExternalContext();

        if (context.getViewRoot() == null) {
            return;
        }

        String viewId = context.getViewRoot().getViewId(); // e.g. "/app/Settings/settingsPage.xhtml"

        // Define allowed pages to skip (public or login)
        boolean isPublicPage = viewId.contains("login.xhtml")
                || viewId.contains("access_denied.xhtml")
                || viewId.contains("sublogin.xhtml")
                || viewId.contains("BiometricLogin.xhtml")
                || viewId.contains("developerPage.xhtml")
                || viewId.contains("public/");

        if (isPublicPage) {
            return;
        }

        // Fetch session bean via EL for safety (recommended)
        UserSession session = context.getApplication()
                .evaluateExpressionGet(context, "#{userSession}", UserSession.class);

        if (session == null || !session.hasAccessTo(viewId)) {
            try {
                var nav = FacesContext.getCurrentInstance().getApplication().getNavigationHandler();
                nav.handleNavigation(context, null, "/WEB-INF/error/access_denied.xhtml");
                // externalContext.redirect(externalContext.getRequestContextPath() + "/app/access-denied.xhtml");
                //context.responseComplete(); // Stop JSF lifecycle
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW; // Runs early in the JSF lifecycle
    }

//    @Override
//    public PhaseId getPhaseId() {
//        return PhaseId.INVOKE_APPLICATION;
//    }
}
