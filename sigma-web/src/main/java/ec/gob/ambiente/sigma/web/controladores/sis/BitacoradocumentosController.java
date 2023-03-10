/**
@autor proamazonia [Christian Báez]  1 dic. 2022

**/
package ec.gob.ambiente.sigma.web.controladores.sis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import ec.gob.ambiente.sigma.ejb.entidades.Project;
import ec.gob.ambiente.sigma.ejb.entidades.sis.Documentslog;
import ec.gob.ambiente.sigma.ejb.facades.ProjectFacade;
import ec.gob.ambiente.sigma.ejb.facades.sis.DocumentslogFacade;
import ec.gob.ambiente.sigma.web.beans.BitacoradocumentosBean;
import ec.gob.ambiente.sigma.web.security.LoginBean;
import ec.gob.ambiente.sigma.web.utils.sis.Mensaje;
import lombok.Getter;

@Named()
@ViewScoped
public class BitacoradocumentosController implements Serializable{

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(BitacoradocumentosController.class);
	
	@Inject
	@Getter
	private BitacoradocumentosBean bitacora;
	
	@Inject
	@Getter
    private MensajesController mensajesController;
	
	@Getter
	@Inject
	private LoginBean loginBean;
	
	@EJB
	@Getter
	private ProjectFacade projectsFacade;
	
	@EJB
	@Getter
	private DocumentslogFacade documentLogServicio;
	
	public void habilitaBuscaFechas(){
		if(getBitacora().getTipoBusqueda()==1){
			getBitacora().setHabilitaPorFecha(true);
			getBitacora().setFechaDesde(null);
			getBitacora().setFechaHasta(null);
		}else{
			getBitacora().setHabilitaPorFecha(false);
		}
	}
	
	public void habilitaPorProyecto(){
		if(getBitacora().getTipoOficio()==1){
			getBitacora().setHabilitaPorProyecto(true);
			getBitacora().setHabilitaPorDestinatario(false);
		}
		else{
			getBitacora().setHabilitaPorProyecto(false);
			getBitacora().setHabilitaPorDestinatario(true);
		}
	}
	
	@PostConstruct
	public void init(){
		try{
			getBitacora().setListadoPDIProgramas(new ArrayList<>());
			getBitacora().setListaBitacoras(new ArrayList<>());
			getBitacora().setListaProyectos(new ArrayList<>());
			getBitacora().setListaProyectos(getProjectsFacade().buscarTodosLosProyectos());
			getBitacora().setTipoBusqueda(1);
			getBitacora().setTipoOficio(1);
			getBitacora().setHabilitaPorProyecto(true);
			getBitacora().setBitacoraDocumentos(new Documentslog());
			getBitacora().setHabilitaPorFecha(true);
			getBitacora().setProyectoSeleccionado(new Project());
			getBitacora().setFiltroPorProyecto(false);
			getBitacora().setFiltroPorOtro(false);
			getBitacora().setFiltroNroOficio(false);
			getBitacora().setFiltroInstitucion(false);
			getBitacora().setFiltroRemitente(false);
			for (Project project: getBitacora().getListaProyectos()) {
				getBitacora().getListadoPDIProgramas().add(project.getProjShortName());
			}
		}catch(Exception e){
			LOG.error(new StringBuilder().append(this.getClass().getName() + "." + "init" + ": ").append(e.getMessage()));
		}
	}
	public void nuevoRegistro(){
		getBitacora().setBitacoraDocumentos(new Documentslog());	
		getBitacora().setListadoPDIProgramasSeleccionados(new ArrayList<>());
		Mensaje.verDialogo("dlgNuevaBitacora");
	}
	/**
	 * Graba una nueva bitacora
	 */
	public void grabarBitacora(){
		try{
			getBitacora().setListaBitacoras(new ArrayList<>());			
			getBitacora().getBitacoraDocumentos().setDcloStatus(true);
			getBitacora().getBitacoraDocumentos().setDcloCreationDate(new Date());
			getBitacora().getBitacoraDocumentos().setDcloCreatorUser(getLoginBean().getUser().getUserName());
			Documentslog docLog= getBitacora().getBitacoraDocumentos();
			if(getBitacora().getTipoOficio()==1){
				for (String proj : getBitacora().getListadoPDIProgramasSeleccionados()) {
					for (Project p : getBitacora().getListaProyectos()) {
						if(proj.equals(p.getProjShortName())){
							getBitacora().setBitacoraDocumentos(new Documentslog());
							getBitacora().getBitacoraDocumentos().setDcloAddressee(docLog.getDcloAddressee());
							getBitacora().getBitacoraDocumentos().setDcloCreationDate(docLog.getDcloCreationDate());
							getBitacora().getBitacoraDocumentos().setDcloCreatorUser(docLog.getDcloCreatorUser());
							getBitacora().getBitacoraDocumentos().setDcloDocumentNumber(docLog.getDcloDocumentNumber());
							getBitacora().getBitacoraDocumentos().setDcloInstitution(docLog.getDcloInstitution());
							getBitacora().getBitacoraDocumentos().setDcloSendDate(docLog.getDcloSendDate());
							getBitacora().getBitacoraDocumentos().setDcloSender(docLog.getDcloSender());
							getBitacora().getBitacoraDocumentos().setDcloSenderCharge(docLog.getDcloSenderCharge());
							getBitacora().getBitacoraDocumentos().setDcloReferringInstitution(docLog.getDcloReferringInstitution());
							getBitacora().getBitacoraDocumentos().setDcloCharge(docLog.getDcloCharge());
							getBitacora().getBitacoraDocumentos().setDcloStatus(docLog.isDcloStatus());
							getBitacora().getBitacoraDocumentos().setDcloSubject(docLog.getDcloSubject());
							getBitacora().getBitacoraDocumentos().setProjects(p);
							getBitacora().getListaBitacoras().add(getBitacora().getBitacoraDocumentos());
							break;
						}
					}
				}
//				getBitacora().getBitacoraDocumentos().setProjects(getBitacora().getProyectoSeleccionado());
				getDocumentLogServicio().agregarActualizar(getBitacora().getListaBitacoras());
			}else{
				getBitacora().getBitacoraDocumentos().setProjects(null);
				getDocumentLogServicio().agregarActualizar(getBitacora().getBitacoraDocumentos());
			}
			getBitacora().setProyectoSeleccionado(null);
			getBitacora().setListaBitacoras(getDocumentLogServicio().listaBitacorasFiltros(getBitacora().getFechaDesde(), getBitacora().getFechaHasta(), getBitacora().getProyectoSeleccionado(), getBitacora().getValorFiltroOtro(),
					getBitacora().getValorFiltroNroOficio(), getBitacora().getValorFiltroRemitente(), getBitacora().getValorFiltroAsunto(), getBitacora().getValorFiltroInstitucion()));
			Mensaje.ocultarDialogo("dlgNuevaBitacora");
			Mensaje.verMensaje(FacesMessage.SEVERITY_INFO,   "",getMensajesController().getPropiedad("info.infoGrabada"));
			Mensaje.actualizarComponente(":form:growl");
		}catch(Exception e){
			LOG.error(new StringBuilder().append(this.getClass().getName() + "." + "grabarBitacora" + ": ").append(e.getMessage()));
		}
	}
	/**
	 * Busqueda de bitacoras
	 */
	public void buscaBitacoras(){
		try{
			getBitacora().setListaBitacoras(getDocumentLogServicio().listaBitacorasFiltros(getBitacora().getFechaDesde(), getBitacora().getFechaHasta(), getBitacora().getProyectoSeleccionado(), getBitacora().getValorFiltroOtro(),
							getBitacora().getValorFiltroNroOficio(), getBitacora().getValorFiltroRemitente(), getBitacora().getValorFiltroAsunto(), getBitacora().getValorFiltroInstitucion()));
			if(getBitacora().getListaBitacoras().size()==0){
				Mensaje.verMensaje(FacesMessage.SEVERITY_INFO,   "",getMensajesController().getPropiedad("info.noInformacion"));
				Mensaje.actualizarComponente(":form:growl");
			}
		}catch(Exception e){
			LOG.error(new StringBuilder().append(this.getClass().getName() + "." + "buscaBitacoras" + ": ").append(e.getMessage()));
		}
	}
	
//	public void editarRegistro(Documentslog bitacora){
//		
//		if(bitacora.getProjects()!=null){
//			getBitacora().setProyectoSeleccionado(bitacora.getProjects());
//			getBitacora().setTipoOficio(1);
//			getBitacora().setHabilitaPorDestinatario(false);
//			getBitacora().setHabilitaPorProyecto(true);
//		}else{
//			getBitacora().setTipoOficio(2);
//			getBitacora().setHabilitaPorDestinatario(true);
//			getBitacora().setHabilitaPorProyecto(false);
//		}
//		getBitacora().setBitacoraDocumentos(bitacora);
//		Mensaje.verDialogo("dlgNuevaBitacora");		
//	}
	public void capturaProyectosSeleccionados(){
		getBitacora().setListadoPDIProgramasSeleccionados(new ArrayList<>());
		for (Project proyecto : getBitacora().getListaProyectos()) {
			if(proyecto.getProjId() == getBitacora().getProyectoSeleccionado().getProjId()){
				getBitacora().getListadoPDIProgramasSeleccionados().add(proyecto.getProjShortName());
				break;
			}
		}
		getBitacora().setListadoPDIProgramasSeleccionadosAux(getBitacora().getListadoPDIProgramasSeleccionados());
	}
	public void editarRegistro(){
		getBitacora().setProyectoSeleccionado(new Project());
		getBitacora().setListadoPDIProgramasSeleccionadosAux(new ArrayList<>());		
		if(getBitacora().getBitacoraDocumentos().getProjects()== null){
//			System.out.println(getBitacora().getBitacoraDocumentos().getProjects().getProjShortName());
			getBitacora().setProyectoSeleccionado(null);
			getBitacora().setTipoOficio(2);
			getBitacora().setHabilitaPorDestinatario(true);
			getBitacora().setHabilitaPorProyecto(false);
			Mensaje.verDialogo("dlgNuevaBitacora");		
		}else{
//			System.out.println(getBitacora().getBitacoraDocumentos().getProjects().getProjShortName());
			getBitacora().setProyectoSeleccionado(getBitacora().getBitacoraDocumentos().getProjects());
			getBitacora().setTipoOficio(1);
			getBitacora().setHabilitaPorDestinatario(false);
			getBitacora().setHabilitaPorProyecto(true);
			capturaProyectosSeleccionados();
			Mensaje.verDialogo("dlgNuevaBitacora");
		}
	}
	public void actualizaBitacora(){
		boolean encontrado=false;

	}
	
	public void eliminarRegistro(){
		try{
			getBitacora().getBitacoraDocumentos().setDcloStatus(false);
			getBitacora().getBitacoraDocumentos().setDcloUpdateDate(new Date());
			getBitacora().getBitacoraDocumentos().setDcloUpdateUser(getLoginBean().getUser().getUserName());
			getDocumentLogServicio().agregarActualizar(getBitacora().getBitacoraDocumentos());
			getBitacora().getListaBitacoras().remove(getBitacora().getBitacoraDocumentos());
			Mensaje.verMensaje(FacesMessage.SEVERITY_INFO,   "",getMensajesController().getPropiedad("info.eliminar"));
			Mensaje.actualizarComponente(":form:growl");
		}catch(Exception e){
			LOG.error(new StringBuilder().append(this.getClass().getName() + "." + "eliminarRegistro" + ": ").append(e.getMessage()));
		}
	}
	public void cancelaEdicionNuevo(){
		getBitacora().setProyectoSeleccionado(null);
		getBitacora().setBitacoraDocumentos(null);
	}
	
	public void mostrarDlgEliminar(){
		Mensaje.verDialogo("dlgEliminaDatosTabla");
	}
	
	public void vaciaOtro(){
		if(getBitacora().isFiltroPorOtro())
			getBitacora().setValorFiltroOtro(null);
	}
	public void vaciaProyecto(){
		if(!getBitacora().isFiltroPorProyecto())
			getBitacora().setProyectoSeleccionado(null);
	}
	public void vaciaOficio(){
		if(getBitacora().isFiltroNroOficio())
			getBitacora().setValorFiltroNroOficio(null);
	}
	public void vaciaRemitente(){
		if(getBitacora().isFiltroRemitente())
			getBitacora().setValorFiltroRemitente(null);
	}
	public void vaciaAsunto(){
		if(getBitacora().isFiltroPorAsunto())
			getBitacora().setValorFiltroAsunto(null);
	}
	public void vaciaInstitucion(){
		if(getBitacora().isFiltroInstitucion())
			getBitacora().setValorFiltroInstitucion(null);
	}
}

