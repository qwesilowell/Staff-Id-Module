/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.util;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.faces.FacesException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.FacesMessage.Severity;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UISelectItems;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.html.HtmlSelectOneMenu;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import static org.apache.commons.lang3.StringUtils.capitalize;
import org.primefaces.PrimeFaces;

import org.primefaces.component.calendar.Calendar;
import org.primefaces.component.inputmask.InputMask;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.primefaces.component.spacer.Spacer;

/**
 *
 * @author Ernest
 */
public class JSF {

    private static final UISelectItems EMPTY_UI_SELECT_ITEMS = new UISelectItems();
    private static final List<SelectItem> EMPTY_SELECT_ITEM = new ArrayList<>(0);

    public static void addSuccessMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, message));
    }

    public static void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
    }

    public static void addWarningMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, message, message));
    }

    public static void addDynamicMessage(String message, boolean type) {
        if (type) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, message));
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
        }
    }

    public static void removeSessionMapValue(String key) {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(key);
    }

    public static Object getRequestMapValue(String key) {
        return FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get(key);
    }

    public static void setRequestMapValue(String key, Object value) {
        FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put(key, value);
    }

    public static Object getSessionMapValue(String key) {
        return FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(key);
    }

    public static void setSessionMapValue(String key, Object value) {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(key, value);

    }

    public static HttpServletRequest getHttpServletRequest() {
        return (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
    }

    public static HttpServletResponse getHttpServletResponse() {
        return (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
    }

    public static HttpServletRequest httpRequest() {
        return getHttpServletRequest();
    }

    public static HttpServletResponse httpResponse() {
        return getHttpServletResponse();
    }

    public static HttpServletRequest request() {
        return getHttpServletRequest();
    }

    public static HttpServletResponse response() {
        return getHttpServletResponse();
    }

    public static String getApplicationUri() {
        try {
            FacesContext ctxt = FacesContext.getCurrentInstance();
            ExternalContext ext = ctxt.getExternalContext();
            URI uri = new URI(
                    ext.getRequestScheme(),
                    null,
                    ext.getRequestServerName(),
                    ext.getRequestServerPort(),
                    ext.getRequestContextPath(),
                    null,
                    null);
            return uri.toASCIIString();
        } catch (URISyntaxException e) {
            throw new FacesException(e);
        }
    }

    public static void successMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, message));
    }

    public static void errorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
    }

    public static void warningMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, message, message));
    }

    public static void message(String summary, String detail, Severity severity) {
        summary = summary == null ? "" : summary;
        detail = detail == null ? "" : detail;
        if (severity != null) {
            FacesMessage message = new FacesMessage(severity, summary, detail);
            facesContext().addMessage(null, message);
        }
    }

    public static void message(String text, Severity severity) {
        message(text, text, severity);
    }

    public static void info(String title, String message) {
        message(title, message, FacesMessage.SEVERITY_INFO);
    }

    public static void warning(String title, String message) {
        message(title, message, FacesMessage.SEVERITY_WARN);
    }

    public static void error(String title, String message) {
        message(title, message, FacesMessage.SEVERITY_ERROR);
    }

    public static void fatal(String title, String message) {
        message(title, message, FacesMessage.SEVERITY_FATAL);
    }

    public static void info(String message) {
        message(message, FacesMessage.SEVERITY_INFO);
    }

    public static void warning(String message) {
        message(message, FacesMessage.SEVERITY_WARN);
    }

    public static void error(String message) {
        message(message, FacesMessage.SEVERITY_ERROR);
    }

    public static void fatal(String message) {
        message(message, FacesMessage.SEVERITY_FATAL);
    }

    /**
     * Classe interna responsável por agrupar os métodos relacionados a
     * apresentação de mensagens.
     *
     * @author thiago-amm
     */
    public static final class message {

        public static void info(String message) {
            message(message, FacesMessage.SEVERITY_INFO);
        }

        public static void warning(String message) {
            message(message, FacesMessage.SEVERITY_WARN);
        }

        public static void error(String message) {
            message(message, FacesMessage.SEVERITY_ERROR);
        }

        public static void fatal(String message) {
            message(message, FacesMessage.SEVERITY_FATAL);
        }

    }

//   public static FacesContext getRequestContext() {
//      return PrimeFacesContext.getCurrentInstance();
//   }
    public static PrimeFaces requestContext() {
        return PrimeFaces.current();
    }

    public static void execute(String script) {
        requestContext().executeScript(script);
    }

    public static void update(String name) {
        requestContext().executeScript(name);
    }

    public static FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    public static FacesContext facesContext() {
        return getFacesContext();
    }

    public static ExternalContext getExternalContext() {
        return facesContext().getExternalContext();
    }

    public static ExternalContext externalContext() {
        return getExternalContext();
    }

    public static void redirect(String url) {
        try {
            externalContext().redirect(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void forward(String url) {
        try {
            externalContext().dispatch(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void invalidateSession() {
        externalContext().invalidateSession();
    }

    public static HttpSession getHttpSession(boolean create) {
        HttpSession session = (HttpSession) externalContext().getSession(create);
        return session;
    }

    public static HttpSession getHttpSession() {
        return getHttpSession(false);
    }

    public static HttpSession httpSession(boolean create) {
        return getHttpSession(create);
    }

    public static HttpSession httpSession() {
        return httpSession(false);
    }

    public static Map<String, String> params() {
        return externalContext().getRequestParameterMap();
    }

    public static ServletContext getServletContext() {
        return httpSession().getServletContext();
    }

    public static ServletContext getContext(String name) {
        return getServletContext().getContext(name);
    }

    public static ServletContext context(String name) {
        return getContext(name);
    }

    public static ServletContext servletContext() {
        return getServletContext();
    }

    public static String getContextName() {
        return externalContext().getContextName();
    }

    public static String contextName() {
        return externalContext().getContextName();
    }

    public static void openDialog(String url, Map<String, Object> options, Map<String, List<String>> params) {
        url = url == null ? "" : url;
        if (!url.isEmpty()) {
            if (options == null) {
                options = new HashMap<>();
                options.put("modal", true);
                options.put("resizable", false);
                options.put("closable", true);
                options.put("draggable", false);
                options.put("contentWidth", 945);
                options.put("contentHeight", 580);
            }
            requestContext().dialog().openDynamic(url, options, params);
        }
    }

    public static void openDialog(String url, Map<String, Object> options) {
        openDialog(url, options, null);
    }

    public static void openDialog(String url) {
        openDialog(url, null);
    }

    public static void dialog(String url) {
        openDialog(url);
    }

    public static void dialog(String url, Map<String, Object> options) {
        openDialog(url, options);
    }

    public static void dialog(String url, Map<String, Object> options, Map<String, List<String>> params) {
        openDialog(url, options, params);
    }

    public static void dialog(String url, boolean modal, boolean resizable, boolean closable, boolean draggable, int width, int height) {
        Map<String, Object> options = new HashMap<>();
        options.put("modal", modal);
        options.put("resizable", resizable);
        options.put("closable", closable);
        options.put("draggable", draggable);
        options.put("contentWidth", width);
        options.put("contentHeight", height);
        dialog(url, options);
    }

    public static void closeDialog(Object data) {
        requestContext().dialog().closeDynamic(data);
    }

    public static void closeDialog() {
        closeDialog(null);
    }

    /**
     * Classe interna responsável por agrupar métodos associados a exibição de
     * janelas.
     *
     * @author thiago-amm
     */
    public static final class dialog {

        public static void close(Object data) {
            closeDialog(data);
        }

    }

    public static void modal(String url, boolean resizable, boolean closable, boolean draggable, int width, int height) {
        dialog(url, true, resizable, closable, draggable, width, height);
    }

    public static void modal(String url, int width, int height) {
        modal(url, false, false, false, width, height);
    }

    public static void modal(String url, int width, int height, boolean closable) {
        modal(url, false, closable, false, width, height);
    }

    public static void modal(String url, boolean closable) {
        modal(url, false, closable, false, 700, 500);
    }

    public static void modal(String url) {
        modal(url, false, false, false, 700, 500);
    }

    public static String getRemoteUser() {
        return externalContext().getRemoteUser();
    }

    public static String remoteUser() {
        return getRemoteUser();
    }

    public static String getContextURL() {
        try {
            String scheme = externalContext().getRequestScheme();
            String host = externalContext().getRequestServerName();
            int port = externalContext().getRequestServerPort();
            String path = externalContext().getRequestContextPath();
            URI uri = new URI(scheme, null, host, port, path, null, null);
            return uri.toASCIIString();
        } catch (URISyntaxException e) {
            throw new FacesException(e);
        }
    }

    /**
     * @param expr
     * @return
     * @author thiago
     */
    public static UIComponent findComponent(String expr) {
        UIComponent component = null;
        if (expr == null) {
            component = FacesContext.getCurrentInstance().getViewRoot().findComponent(expr);
        }
        return component;
    }

    public static UIComponent component(String expr) {
        return findComponent(expr);
    }

    public static <T> T component(String id, Class<T> clazz) {
        return clazz.cast(component(id));
    }

    public static InputMask getInputMask(String id) {
        return component(id, InputMask.class);
    }

    public static InputMask inputMask(String id) {
        return getInputMask(id);
    }

    public static InputText getInputText(String id) {
        return component(id, InputText.class);
    }

    public static InputText inputText(String id) {
        return getInputText(id);
    }

    public static HtmlSelectOneMenu getHtmlSelectOneMenu(String id) {
        return component(id, HtmlSelectOneMenu.class);
    }

    public static HtmlSelectOneMenu htmlSelectOneMenu(String id) {
        return getHtmlSelectOneMenu(id);
    }

    public static SelectOneMenu getSelectOneMenu(String id) {
        return component(id, SelectOneMenu.class);
    }

    public static SelectOneMenu selectOneMenu(String id) {
        return getSelectOneMenu(id);
    }

    public static Calendar getCalendar(String id) {
        return component(id, Calendar.class);
    }

    public static Calendar calendar(String id) {
        return getCalendar(id);
    }

    public static Spacer getSpacer(String id) {
        return component(id, Spacer.class);
    }

    public static Spacer spacer(String id) {
        return getSpacer(id);
    }

//    public static <T> List<SelectItem> selectItems(List<T> list) {
//        List<SelectItem> selectItems = EMPTY_SELECT_ITEM;
//        if (isNotNull(list)) {
//            selectItems = new ArrayList<>();
//        }
//        return selectItems;
//    }

//    public static SelectItem selectItem(String label, Object value) {
//        SelectItem selectItem = null;
//        if (isNotEmptyOrNull(label)) {
//            selectItem = new SelectItem();
//            selectItem.setLabel(label);
//            selectItem.setValue(value);
//        }
//        return selectItem;
//    }

//    public static <T> UISelectItems uiSelectItems(List<T> list) {
//        UISelectItems uiSelectItems = EMPTY_UI_SELECT_ITEMS;
//        if (isNotNull(list)) {
//            uiSelectItems = new UISelectItems();
//            List<SelectItem> selectItems = selectItems(list);
//            SelectItem selecione = selectItem("Selecione", "");
//            selecione.setNoSelectionOption(true);
//            selectItems.add(selecione);
//            SelectItem selectItem = null;
//            for (T i : list) {
//                if (i instanceof SelectOption) {
//                    SelectOption o = (SelectOption) i;
//                    selectItem = selectItem(o.getLabel(), o.getValue());
//                } else {
//                    selectItem = selectItem(capitalize(i.toString()), i);
//
//                }
//                selectItems.add(selectItem);
//            }
//            uiSelectItems.setValue(selectItems);
//        }
//        return uiSelectItems;
//    }

//    public static <T> void selectOneMenu(SelectOneMenu selectOneMenu, List<T> list) {
//        if (notContainsNull(selectOneMenu, list)) {
//            selectOneMenu.getChildren().add(uiSelectItems(list));
//        }
//    }

    @SafeVarargs
    public static <T> void selectOneMenu(SelectOneMenu selectOneMenu, T... array) {
        selectOneMenu(selectOneMenu, Arrays.asList(array));
    }

//    public static <T> SelectOneMenu selectOneMenu(String id, List<T> list) {
//        SelectOneMenu selectOneMenu = null;
//        if (isNotNull(list)) {
//            if (isEmptyOrNull(id)) {
//                selectOneMenu = new SelectOneMenu();
//            } else {
//                selectOneMenu = JSF.selectOneMenu(id);
//            }
//            selectOneMenu.getChildren().add(uiSelectItems(list));
//        }
//        return selectOneMenu;
//    }

    public static <T> SelectOneMenu selectOneMenu(List<T> list) {
        return selectOneMenu((String) null, list);
    }

    @SafeVarargs
    public static <T> SelectOneMenu selectOneMenu(String id, T... array) {
        return selectOneMenu(id, Arrays.asList(array));
    }

    @SafeVarargs
    public static <T> SelectOneMenu selectOneMenu(T... array) {
        return selectOneMenu((String) null, Arrays.asList(array));
    }

    public static void clear(SelectOneMenu selectOneMenu) {
        if (selectOneMenu != null) {
            selectOneMenu.getChildren().clear();
        }
    }

    /**
     * @param list
     * @param labelMethod
     * @param valueMethod
     * @return
     * @author thiago
     */
//    public static <T> SelectOneMenu selectOneMenu(List<T> list, String labelMethod, String valueMethod) {
//        SelectOneMenu selectOneMenu = null;
//        if (notContainsNull(list, labelMethod, valueMethod)) {
//            selectOneMenu = new SelectOneMenu();
//            UISelectItems uiSelectItems = new UISelectItems();
//            List<SelectItem> selectItems = new ArrayList<>();
//            try {
//                for (T i : list) {
//                    SelectItem selectItem = new SelectItem();
//                    Method label = i.getClass().getMethod(labelMethod, (Class<?>[]) null);
//                    Method value = i.getClass().getMethod(valueMethod, (Class<?>[]) null);
//                    selectItem.setLabel(label.invoke(i, (Object[]) null).toString());
//                    selectItem.setValue(value.invoke(i, (Object[]) null).toString());
//                    selectItems.add(selectItem);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            uiSelectItems.setValue(selectItems);
//            selectOneMenu.getChildren().add(uiSelectItems);
//        }
//        return selectOneMenu;
//    }

    public static <T> SelectOneMenu selectOneMenu(Object[] array, String labelMethod, String valueMethod) {
        if (array != null) {
            return selectOneMenu(Arrays.asList(array), labelMethod, valueMethod);
        }
        return null;
    }

//    public static <T> HtmlSelectOneMenu htmlSelectOneMenu(List<T> list) {
//        HtmlSelectOneMenu htmlSelectOneMenu = null;
//        if (isNotNull(list)) {
//            htmlSelectOneMenu = new HtmlSelectOneMenu();
//            UISelectItems uiSelectItems = new UISelectItems();
//            List<SelectItem> selectItems = new ArrayList<>();
//            for (T i : list) {
//                SelectItem selectItem = new SelectItem();
//                selectItem.setLabel(i.toString());
//                selectItem.setValue(i);
//                selectItems.add(selectItem);
//            }
//            uiSelectItems.setValue(selectItems);
//            htmlSelectOneMenu.getChildren().add(uiSelectItems);
//        }
//        return htmlSelectOneMenu;
//    }

    @SafeVarargs
    public static <T> HtmlSelectOneMenu htmlSelectOneMenu(T... array) {
        return htmlSelectOneMenu(Arrays.asList(array));
    }

//    public static <T> HtmlSelectOneMenu htmlSelectOneMenu(List<T> list, String labelMethod, String valueMethod) {
//        HtmlSelectOneMenu htmlSelectOneMenu = null;
//        if (notContainsNull(list, labelMethod, valueMethod)) {
//            htmlSelectOneMenu = new HtmlSelectOneMenu();
//            UISelectItems uiSelectItems = new UISelectItems();
//            List<SelectItem> selectItems = new ArrayList<>();
//            try {
//                for (T i : list) {
//                    SelectItem selectItem = new SelectItem();
//                    Method label = i.getClass().getMethod(labelMethod, (Class<?>[]) null);
//                    Method value = i.getClass().getMethod(valueMethod, (Class<?>[]) null);
//                    selectItem.setLabel(label.invoke(i, (Object[]) null).toString());
//                    selectItem.setValue(value.invoke(i, (Object[]) null).toString());
//                    selectItems.add(selectItem);
//                }
//            } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
//                e.printStackTrace();
//            }
//            uiSelectItems.setValue(selectItems);
//            htmlSelectOneMenu.getChildren().add(uiSelectItems);
//        }
//        return htmlSelectOneMenu;
//    }

    public static <T> HtmlSelectOneMenu htmlSelectOneMenu(Object[] array, String labelMethod, String valueMethod) {
        if (array != null) {
            return htmlSelectOneMenu(Arrays.asList(array), labelMethod, valueMethod);
        }
        return null;
    }
//
//    public static DataTable dataTable(String id) {
//        return (DataTable) component(id);
//    }

    public static void renderResponse() {
        facesContext().renderResponse();
    }

    public static void responseComplete() {
        facesContext().responseComplete();
    }

    public static UIViewRoot getViewRoot() {
        return facesContext().getViewRoot();
    }

    public static UIViewRoot viewRoot() {
        return getViewRoot();
    }

    public static UIViewRoot root() {
        return viewRoot();
    }

    public interface SelectOption {

        String getLabel();

        Object getValue();
    }
//
//    public static Tab tab(String id) {
//        Tab tab = (Tab) JSF.component(id);
//        return tab;
//    }
//
//    public static TabView tabView(String id) {
//        TabView tabView = (TabView) JSF.component(id);
//        return tabView;
//    }

    public static String getRequestURL() {
        return JSF.request().getRequestURL().toString();
    }

    public static String requestURL() {
        return getRequestURL();
    }

    public static String getRequestURI() {
        return JSF.request().getRequestURI();
    }

    public static String requestURI() {
        return getRequestURI();
    }
}
