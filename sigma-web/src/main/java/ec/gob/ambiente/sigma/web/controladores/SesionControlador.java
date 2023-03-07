/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.ambiente.sigma.web.controladores;

import java.io.IOException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.primefaces.PrimeFaces;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;

import ec.gob.ambiente.client.SuiaServices;
import ec.gob.ambiente.client.SuiaServices_Service;
import ec.gob.ambiente.sigma.ejb.entidades.User;
import ec.gob.ambiente.sigma.ejb.facades.ProjectFacade;
import ec.gob.ambiente.sigma.ejb.facades.ProjectsStrategicPartnerFacade;
import ec.gob.ambiente.sigma.ejb.facades.UserFacade;
import ec.gob.ambiente.sigma.ejb.model.Menu;
import ec.gob.ambiente.sigma.ejb.model.MenuRole;
import ec.gob.ambiente.sigma.ejb.model.MenuVO;
import ec.gob.ambiente.sigma.ejb.model.Role;
import ec.gob.ambiente.sigma.ejb.model.RolesUser;
import ec.gob.ambiente.sigma.ejb.services.MenuFacade;
import ec.gob.ambiente.sigma.ejb.services.MenuRoleFacade;
import ec.gob.ambiente.sigma.ejb.services.RoleFacade;
import ec.gob.ambiente.sigma.ejb.services.RolesUserFacade;
import ec.gob.ambiente.sigma.web.security.LoginBean;
import ec.gob.ambiente.sigma.web.utils.JsfUtil;
import ec.gob.ambiente.sigma.web.utils.sis.enumeraciones.TipoRolesUsuarioEnum;
import ec.gov.sri.wsconsultacontribuyente.ContribuyenteCompleto;
import lombok.Getter;

/**
 *
 * @author Blueit-Participante
 */
@Named(value = "ses")
@SessionScoped
public class SesionControlador implements Serializable {

    private static final Logger LOG = Logger.getLogger(SesionControlador.class.getName());

    @EJB
    private UserFacade usuFacade;
    @EJB
    private RoleFacade rolFacade;
    @EJB
    private RolesUserFacade rolUserFacade;
    @EJB
    private MenuRoleFacade menuRolFacade;

    @Inject
    private AplicacionControlador appBean;
	@EJB
	@Getter
	private ProjectFacade projectsFacade;
	@EJB
	@Getter
	private ProjectsStrategicPartnerFacade projectsStrategicPartnersFacade;

    private User usuarioLogeado;
    private List<RolesUser> listaRolesDeUsuarioLogeado;
    private List<Menu> listaMenusDeRolesDeUsuarioLogeado;
    private MenuModel menuModel;

    private String username;
    private String password;

    private String claveAnterior;
    private String nuevaClave;
    private String nuevaClaveConf;
    private String mailRecuperarClave;
    
    private boolean tieneRolSIGMA;
    private boolean tieneRolSIS;
    private String moduloActual;
    @Inject
	@Getter
	private LoginBean loginBean;
    private ExternalContext ec;

    /**
     * Creates a new instance of SesionControlador
     */
    public SesionControlador() {
    }

    @PostConstruct
    public void inicializar() {
        try {
            
            username = "";
            password = "";
            moduloActual="";
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
   

    public void iniciarSesion() {
        try {
            if (username.length() > 0 && password.length() > 0) {
                usuarioLogeado = usuFacade.obtenerUsuarioConPassword(username, JsfUtil.claveEncriptadaSHA1(password));
                if (usuarioLogeado == null) {
                    addErrorMessage(appBean.getBundle().getString("msg124"));
                    username = "";
                    password = "";
                } else {
                	getLoginBean().setUser(usuarioLogeado);
                	ec = FacesContext.getCurrentInstance().getExternalContext();
    				getLoginBean().setSesion( (HttpSession)ec.getSession(true));
    				getLoginBean().getSesion().setAttribute("logeado", true);
                    listaRolesDeUsuarioLogeado = rolUserFacade.listarRolesDeUsuario(usuarioLogeado);
                    getLoginBean().setListaRolesUsuario(listaRolesDeUsuarioLogeado);
                    mostrarIngresosSegunRoles();
                    redireccionarAPagina("", "preinicio");
                   
                }

            } else {
                addWarningMessage(appBean.getBundle().getString("msg123"));
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
    private void mostrarIngresosSegunRoles(){
    	try{
    		tieneRolSIGMA=false;
    		tieneRolSIS=false;
    		for(RolesUser ru:listaRolesDeUsuarioLogeado){
    			if(ru.getRole().getRoleName().startsWith("SIGMA_")){
    				tieneRolSIGMA=true;
    			}
    			if(ru.getRole().getRoleName().startsWith("SIS_")){
    				tieneRolSIS=true;
    			}
    		}
    	} catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
    public void llenarYMostrarMenu(String subsis){
    	try{
    		List<RolesUser> rolesDeSubSis=new ArrayList<>();
    		
    		for(RolesUser ru:listaRolesDeUsuarioLogeado){
    			if(ru.getRole().getRoleName().startsWith(subsis)){
    				rolesDeSubSis.add(ru);
    			}
        	}
    		
    		listaMenusDeRolesDeUsuarioLogeado=menuRolFacade.listarMenusPorRol(rolesDeSubSis);
            llenarMenu();
            moduloActual=subsis;
            controlRolesSis();
    		redireccionarAPagina("", "inicio");
    	}catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void llenarMenu(){
    	try{
    		menuModel=new DefaultMenuModel();
    		if(!listaMenusDeRolesDeUsuarioLogeado.isEmpty()){
    			for(Menu mr:listaMenusDeRolesDeUsuarioLogeado){
    				
    				if(mr.getMenuEndNode()==false){
    					DefaultSubMenu firstSubmenu = DefaultSubMenu.builder().label(mr.getMenuName()).icon(mr.getMenuIcon()).build();
    					for(Menu mr1:listaMenusDeRolesDeUsuarioLogeado){
    						if(mr1.getMenuEndNode()&&mr1.getMenu()!=null&&mr1.getMenu().getMenuId()==mr.getMenuId()){
    							String menuName=mr1.getMenuName().startsWith("lbl")?appBean.getBundle().getString(mr1.getMenuName()):mr1.getMenuName();
    							DefaultMenuItem item = DefaultMenuItem.builder()
    					                .value(menuName)
    					                .icon(mr1.getMenuIcon())
    					                .command(mr1.getMenuAction())
    					                .update("msgs")
    					                .build();
    					        firstSubmenu.getElements().add(item);
    						}
    					}
    					menuModel.getElements().add(firstSubmenu);
    				}
    			}
    			/*DefaultMenuItem itemSalir = DefaultMenuItem.builder()
		                .value("Salir")
		                .icon("fa fa-power-off")
		                .command("#{ses.cerrarSesion()}")
		                .update("msgs")
		                .build();
    			menuModel.getElements().add(itemSalir);*/
    		}
    	}catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
    public void cerrarSesion() {
        try {
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(Boolean.FALSE);
            session.invalidate();
            redirect("/sigma-web/index.xhtml?faces-redirect=true&redirected=true");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public User getUsuarioLogeado() {
        return usuarioLogeado;
    }

    public void setUsuarioLogeado(User usuarioLogeado) {
        this.usuarioLogeado = usuarioLogeado;
    }

   
    public List<RolesUser> getListaRolesDeUsuarioLogeado() {
		return listaRolesDeUsuarioLogeado;
	}

	public void setListaRolesDeUsuarioLogeado(List<RolesUser> listaRolesDeUsuarioLogeado) {
		this.listaRolesDeUsuarioLogeado = listaRolesDeUsuarioLogeado;
	}

	public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Se encarga de ejecutar una redireccion.
     *
     * @param url url de destino
     * @throws IOException en caso de no poder hacer la redireccion
     */
    public void redirect(final String url) throws IOException {
        FacesContext.getCurrentInstance().getExternalContext().redirect(url);
    }

    public void redireccionarAPagina(String carpeta, String pagina) {
        try {
            if (carpeta.equals("")) {
                redirect("/sigma-web/pages/" + pagina + ".xhtml");
            } else {
                redirect("/sigma-web/pages/" + carpeta + "/" + pagina + ".xhtml");
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
    public void redireccionarAPaginaConParametro(String carpeta, String pagina,String paramName,String param) {
        try {
        	HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
			session.setAttribute(paramName, param);
			
            redireccionarAPagina(carpeta, pagina);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    public void redireccionarAPaginaConParametros(String carpeta, String pagina,String paramName1,String param1,String paramName2,Integer param2) {
        try {
        	HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
			session.setAttribute(paramName1, param1);
			session.setAttribute(paramName2, param2);
			
			redireccionarAPagina(carpeta, pagina);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
    public void redireccionarAPaginaConParametros(String carpeta, String pagina,String paramName1,String param1,String paramName2,String param2, String paramName3,Integer param3) {
        try {
        	HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
			session.setAttribute(paramName1, param1);
			session.setAttribute(paramName2, param2);
			session.setAttribute(paramName3, param3);
			
			redireccionarAPagina(carpeta, pagina);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
    
/*
    public boolean controlNav(String control) {
        boolean b = false;
        try {
            String val = "";
            if (usuarioLogeado == null || usuarioLogeado.getUserId() == null) {
                val = "No se ha logeado.";
            } else {
                if (control.equals("")) {

                } else if (control.equals("ADM")) {
                    boolean tieneRolAdm = false;
                    for (Role r : listaRolesDeUsuarioLogeado) {
                        if (r.getRoleMnemonic().equals("ADM")) {
                            tieneRolAdm = true;
                        }
                    }
                    if (!tieneRolAdm) {
                        val = val + " No tiene Rol Administrador.";
                    }
                } else if (control.equals("USU")) {
                    boolean tieneRolUsu = false;
                    for (Role r : listaRolesDeUsuarioLogeado) {
                        if (r.getRoleMnemonic().equals("USU")) {
                            tieneRolUsu = true;
                        }
                    }
                    if (!tieneRolUsu) {
                        val = val + " No tiene Rol Técnico.";
                    }

                } else {
                    val = "error";
                }
            }

            if (val.equals("")) {
                b = true;
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return b;
    }*/

    public void prepararCambioClave() {
        try {
            claveAnterior="";
            nuevaClave = "";
            nuevaClaveConf = "";
            PrimeFaces.current().executeScript("PF('dlgCambioClave').show()");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    /*
    public void cambiarClave(){
        try{
            String valMsg="";
            if(claveAnterior.length()<8){
                valMsg=valMsg+" Clave anterior no válida.";
            }
            if(nuevaClave.length()<8){
                valMsg=valMsg+" Clave nueva no válida.";
            }else{
                
                if(!usuarioLogeado.getUserPassword().equals(usuFacade.cifrarCadenaTexto(claveAnterior))){
                    valMsg=valMsg+" Clave anterior no coincide.";
                }
                if(!nuevaClave.equals(nuevaClaveConf)){
                    valMsg=valMsg+" La nueva clave no coincide.";
                }
                if(claveAnterior.equals(nuevaClave)){
                    valMsg=valMsg+" Nueva clave similar a la anterior.";
                }
            }
            
            if(valMsg.isEmpty()){
                usuFacade.cambiarClaveUsuario(usuarioLogeado, nuevaClave);
                nuevaClave="";
                nuevaClaveConf="";
                cerrarSesion();
            }else{
                addErrorMessage(valMsg);
            }
        }catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }*/

    public void cancelarCambioClave() {
        try {
            claveAnterior="";
            nuevaClave = "";
            nuevaClaveConf = "";
            PrimeFaces.current().executeScript("PF('dlgCambioClave').hide()");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
    public void prepararReinicioClave(){
        try{
            mailRecuperarClave="";
            PrimeFaces.current().executeScript("PF('dlgRecClave').show()");
        }catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    /*
    public void reiniciarClave(){
        try{
            Utilitario u=new Utilitario();
            mailRecuperarClave=mailRecuperarClave.trim();
            if(u.isValidEmailAddress(mailRecuperarClave)){
                User urec=usuFacade.obtenerUsuEmailRecuperacion(mailRecuperarClave);
                if(urec!=null){
                    String claveRandom="";
                    RandomString gen = new RandomString(8, ThreadLocalRandom.current());
                    claveRandom=gen.nextString();
                    usuFacade.cambiarClaveUsuario(urec, claveRandom);
                    String asunto="Recuperación de clave.";
                    String mensaje="Hemos reiniciado la clave del usuario: "+urec.getUserName()+". Su nueva clave es: "+claveRandom;
                    mensaje=mensaje+ "\n\n Enlace al sistema: http://"+appBean.getIp()+":"+appBean.getPort()+"/eventos";
                    mensaje=mensaje+ "\n\n Si usted no solicitó el reinicio, contáctese con un administrador.";
                    List<String> dest=new ArrayList<>();
                    List<String> cc=new ArrayList<>();
                    dest.add(mailRecuperarClave);
                    appBean.enviarCorreo(appBean.getMail(), dest, cc, asunto, mensaje);
                    addSuccessMessage("Contraseña reiniciada. Verifique su bandeja de correo.");
                }else{
                    addErrorMessage("Email no registrado. Contáctese con un administrador.");
                }
            }else{
                addErrorMessage("Email no válido");
            }
        }catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }*/

    public void addErrorMessage(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"", msg);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

    public void addWarningMessage(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", msg);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

    public void addSuccessMessage(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", msg);
        FacesContext.getCurrentInstance().addMessage("successInfo", facesMsg);
    }

    public void mensajeErrorComponente(String idElemento, String detalle) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(idElemento, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", detalle));
    }

    public String obtenerIp() {
        //String ipCliente = getRequest().getRemoteAddr();
        String ipCliente = getRequest().getHeader("X-FORWARDED-FOR");
        if (ipCliente == null) {
            ipCliente = getRequest().getRemoteAddr();
        }
        return ipCliente;
    }

    public HttpServletRequest getRequest() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        if (request == null) {
            throw new RuntimeException(
                    "No se pudo recuperar HttpServletRequest");
        }
        return request;
    }

    public String getRequestHeader(String nombre) {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        return externalContext.getRequestHeaderMap().get(nombre);
    }
    
    public List<String[]> validacionMsg(String idElemento, String msg1, List<String[]> listaGeneral) {
        try {
            String[] msg = {idElemento, msg1};
            listaGeneral.add(msg);
            return listaGeneral;
        } catch (Exception ex) {
        	
            LOG.log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public void addErrorMessagesForComponentes(List<String[]> listaMsg){
    	for (String[] s : listaMsg) {
            mensajeErrorComponente(s[0], s[1]);
        }
        PrimeFaces.current().focus(listaMsg.get(0)[0]);
    }

    public String getNuevaClave() {
        return nuevaClave;
    }

    public void setNuevaClave(String nuevaClave) {
        this.nuevaClave = nuevaClave;
    }

    public String getNuevaClaveConf() {
        return nuevaClaveConf;
    }

    public void setNuevaClaveConf(String nuevaClaveConf) {
        this.nuevaClaveConf = nuevaClaveConf;
    }

    public String getClaveAnterior() {
        return claveAnterior;
    }

    public void setClaveAnterior(String claveAnterior) {
        this.claveAnterior = claveAnterior;
    }

    public String getMailRecuperarClave() {
        return mailRecuperarClave;
    }

    public void setMailRecuperarClave(String mailRecuperarClave) {
        this.mailRecuperarClave = mailRecuperarClave;
    }
    
	public String obtenerIdUsuarioSocio(){
		String id="";
		
		if(usuarioLogeado!=null&&usuarioLogeado.getUserId()!=null&&!listaRolesDeUsuarioLogeado.isEmpty()){
			for(RolesUser r:listaRolesDeUsuarioLogeado){
				if(r.getRole().getRoleName().contains("SIGMA_SOCIO")){
					if(r.getRousDescription()!=null&&r.getRousDescription().length()>=13){
						id=r.getRousDescription().substring(r.getRousDescription().length() - 13);
					}
				}
			}
		}
		
		return id;
	}
	
	public MenuModel getMenuModel() {
		return menuModel;
	}

	public void setMenuModel(MenuModel menuModel) {
		this.menuModel = menuModel;
	}
	
	public boolean accesoAMenu(String mnemonicPage){
		boolean r=false;
		try{
			//programacion para evaluar rolesDeUsuario con sus menus permitidos
			if(listaMenusDeRolesDeUsuarioLogeado!=null){
			for(Menu m:listaMenusDeRolesDeUsuarioLogeado){
				if(m.getMenuMnemonic().contains(mnemonicPage)){
					r=true;
				}
			}
			}
			
			
			
			
		}catch(Exception ex){
			 LOG.log(Level.SEVERE, null, ex);
		}
		return r;
	}
	
	public boolean visualizacionSegunRol(String nameRol){
		boolean r=false;
		try{
			
			if(listaRolesDeUsuarioLogeado!=null&&!listaRolesDeUsuarioLogeado.isEmpty()){
				for(RolesUser rolu:listaRolesDeUsuarioLogeado){
					if(rolu.getRole().getRoleName().contains(nameRol)){
						r=true;
					}
				}
			}
			
			
		}catch(Exception ex){
			 LOG.log(Level.SEVERE, null, ex);
		}
		return r;
	}
	
	public ContribuyenteCompleto obtenerContribuyenteDesdeWSSri(String ruc){
		ContribuyenteCompleto c=new ContribuyenteCompleto();
		try{
			SuiaServices_Service service1 = new SuiaServices_Service();
	        SuiaServices port1 = service1.getSuiaServicesPort();
	        c=port1.getRuc(appBean.getSigmaWebServicesUsername(), appBean.getSigmaWebServicesPassword(), ruc);
		}catch(Exception ex){
			 LOG.log(Level.SEVERE, null, ex);
		}
		return c;
	}

	public List<Menu> getListaMenusDeRolesDeUsuarioLogeado() {
		return listaMenusDeRolesDeUsuarioLogeado;
	}

	public void setListaMenusDeRolesDeUsuarioLogeado(List<Menu> listaMenusDeRolesDeUsuarioLogeado) {
		this.listaMenusDeRolesDeUsuarioLogeado = listaMenusDeRolesDeUsuarioLogeado;
	}

	public boolean getTieneRolSIGMA() {
		return tieneRolSIGMA;
	}

	public void setTieneRolSIGMA(boolean tieneRolSIGMA) {
		this.tieneRolSIGMA = tieneRolSIGMA;
	}

	public boolean getTieneRolSIS() {
		return tieneRolSIS;
	}

	public void setTieneRolSIS(boolean tieneRolSIS) {
		this.tieneRolSIS = tieneRolSIS;
	}

	public String getModuloActual() {
		return moduloActual;
	}

	public void setModuloActual(String moduloActual) {
		this.moduloActual = moduloActual;
	}

	////////////////SIS////////////////////////////
	
	public void controlRolesSis(){
		try{
		if(getLoginBean().getListaRolesUsuario().size()>1){
			for (RolesUser ru : getLoginBean().getListaRolesUsuario()) {						
				if(ru.getRole().getRoleName().equals(TipoRolesUsuarioEnum.SIS_socio_implementador.getEtiqueta())){
					getLoginBean().setListaProyectosDelSocioImplementador(getProjectsFacade().listarProyectosSocioImplementador(ru.getRousDescription()));
					getLoginBean().setTipoRol(2);
					break;
				}
			}
		}else{
			for (RolesUser ru : getLoginBean().getListaRolesUsuario()) {
				if(ru.getRole().getRoleName().equals(TipoRolesUsuarioEnum.SIS_tecnico.getEtiqueta())){
					getLoginBean().setTipoRol(4);
					break;
				}else if(ru.getRole().getRoleName().equals(TipoRolesUsuarioEnum.SIS_socio_estrategico.getEtiqueta())){
					getLoginBean().setListaProyectosDelSocioEstrategico(getProjectsStrategicPartnersFacade().listaProyectosSocioEstrategico(ru.getRousDescription()));
					getLoginBean().setTipoRol(3);
					break;
				}else if(ru.getRole().getRoleName().equals(TipoRolesUsuarioEnum.SIS_socio_implementador.getEtiqueta())){
					getLoginBean().setListaProyectosDelSocioImplementador(getProjectsFacade().listarProyectosSocioImplementador(ru.getRousDescription()));
					getLoginBean().setTipoRol(2);
					break;
				}else{
					getLoginBean().setTipoRol(1);
					break;
				}
			}
			
		}
		}catch(Exception e){
			LOG.log(Level.SEVERE, null, e);
		}
	}

	/*
	 *
	public void fillMenuModel() 
	{
		//TODO colocar el código de la generación del menú aquí
		String mnemonic = (String) JsfUtil.getProperty("roles.mnemonic", true);
		for (RolesUser roleUser : rolesUsers) {
			if(!roleUser.getRole().getRoleName().startsWith(prefijoRoles))
			{
				Role r =roleFacade.findByName(prefijoRoles+roleUser.getRole().getRoleName());
				if(r!=null)
				{
					roleUser.setRole(r);
				}					
				else
				{					
					Role rolSujetoControl =roleFacade.findByName(roleUser.getRole().getRoleName());
					
					if(rolSujetoControl != null ){
						roleUser.setRole(rolSujetoControl);
					}else{
						System.out.println("No se encontró el rol "+prefijoRoles+roleUser.getRole().getRoleName()+" para generar el menú");
						loginBean.logout();
						return;
					}
				}
			}
		}
		
		List<MenuVO> menus = menuFacade.getMenusByRoles(rolesUsers, mnemonic);
		if(menus==null)
		{
			System.out.println("Error al generar el menú con los roles de Usuario");
			JsfUtil.addErrorMessage("Error al generar el menú con los roles de Usuario");
			loginBean.logout();
			return;
		}
			
		menuModel = new DefaultMenuModel();
		Menu parentMenu = menuFacade.findByMnemonic(mnemonic);
		for (MenuVO menu : menus) {
			
			if(loginBean.getOrganization() == null){
			
				if(menu.getIdMenuParent() != null && menu.getIdMenuParent().equals(parentMenu.getMenuId()))
				{
					if(!menu.getEndNode())
					{
						DefaultSubMenu subMenu = new DefaultSubMenu();
						subMenu.setLabel(menu.getLabelMenu());
						subMenu.setIcon(menu.getIcon());						
						fillItems(menu, menus, subMenu, null);
						menuModel.addElement(subMenu);
					}
					else
					{
						DefaultMenuItem menuItem_ = new DefaultMenuItem();
						menuItem_.setValue(menu.getLabelMenu());
						menuItem_.setUrl(("S/N").equals(menu.getUrlMenu()) ? null : menu.getUrlMenu());					
						menuItem_.setCommand(menu.getActionMenu().isEmpty() ? null : menu.getActionMenu());
						menuItem_.setIcon(menu.getIcon());
						menuModel.addElement(menuItem_);					
					}
				}
			}else{
				if(menu.getIdMenuParent() != null && menu.getIdMenuParent().equals(parentMenu.getMenuId()))
				{
					if(!menu.getLabelMenu().equals("Paso 1: CONVOCATORIA") && !menu.getLabelMenu().equals("Paso 2: EVALUACIÓN")){
						if(!menu.getEndNode())
						{
							DefaultSubMenu subMenu = new DefaultSubMenu();
							subMenu.setLabel(menu.getLabelMenu());
							subMenu.setIcon(menu.getIcon());
							fillItems(menu, menus, subMenu, null);
							menuModel.addElement(subMenu);
						}
						else
						{
							DefaultMenuItem menuItem_ = new DefaultMenuItem();
							menuItem_.setValue(menu.getLabelMenu());
							menuItem_.setUrl(("S/N").equals(menu.getUrlMenu()) ? null : menu.getUrlMenu());					
							menuItem_.setCommand(menu.getActionMenu().isEmpty() ? null : menu.getActionMenu());
							menuItem_.setIcon(menu.getIcon());
							menuModel.addElement(menuItem_);					
						}
					}
				}
			}
		}
		
		loginBean.setMenuModel(menuModel);
	}
	
	private DefaultMenuItem fillItems(MenuVO menu_, List<MenuVO> menus, DefaultSubMenu menuParent, DefaultMenuItem menuItem) 
	{		
		for (MenuVO menu : menus) {
		    if (menu_.getIdMenu().equals(menu.getIdMenuParent())) 
		    {
		        if (menu.getEndNode()) 
		        {
		            DefaultMenuItem menuItem_ = new DefaultMenuItem();
		            menuItem_.setValue(menu.getLabelMenu());
		            boolean result = false;//applicationFacade. .verifyPendingReport(loginBean.getPeopleVo().getDocumentId());
					if(!result)
						menuItem_.setUrl(("S/N").equals(menu.getUrlMenu()) ? null : menu.getUrlMenu());
					else
						menuItem_.setUrl("/pages/application/pendingReport.xhtml");		           
		            menuItem_.setCommand(menu.getActionMenu().isEmpty() ? null : menu.getActionMenu());
		            //menuItem_.setIcon(menu.getIcon());
		            menuItem_.setStyleClass("");
		            menuParent.getElements().add(menuItem_);
	            }
		        else 
		        {
	                addChildElement(menuParent, menu, menus, menuItem);
	            }
	        }
	    }
	    return menuItem;
	}
	
	private void addChildElement(DefaultSubMenu menuParent, MenuVO menu_, List<MenuVO> menus, DefaultMenuItem menuItem) {
        DefaultSubMenu submenuChild = new DefaultSubMenu();
        submenuChild.setLabel(menu_.getLabelMenu());
        menuParent.getElements().add(submenuChild);
        //submenuChild.setIcon(menu_.getIcon());
        submenuChild.setStyleClass("");
        DefaultMenuItem menus_ = fillItems(menu_, menus, submenuChild, menuItem);
        if (menus_ != null) {
            submenuChild.getElements().add(menus_);
        }
    }
   */
	
	
}
