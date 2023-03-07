/**
@autor proamazonia [Christian Báez]  26 may. 2021

**/
package ec.gob.ambiente.sigma.web.beans;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import ec.gob.ambiente.sigma.ejb.dto.sis.DtoComponenteSalvaguarda;
import ec.gob.ambiente.sigma.ejb.entidades.Component;
import ec.gob.ambiente.sigma.ejb.entidades.Partner;
import ec.gob.ambiente.sigma.ejb.entidades.Project;
import ec.gob.ambiente.sigma.ejb.entidades.ProjectsSpecificObjective;
import ec.gob.ambiente.sigma.ejb.entidades.ProjectsStrategicPartner;
import ec.gob.ambiente.sigma.ejb.entidades.Safeguard;
import ec.gob.ambiente.sigma.ejb.entidades.sis.AdvanceExecutionSafeguards;
import ec.gob.ambiente.sigma.ejb.entidades.sis.CatalogTypes;
import ec.gob.ambiente.sigma.ejb.entidades.sis.Catalogs;
import ec.gob.ambiente.sigma.ejb.entidades.sis.Indicators;
import ec.gob.ambiente.sigma.ejb.entidades.sis.ProjectGenderIndicator;
import ec.gob.ambiente.sigma.ejb.entidades.sis.ProjectQuestions;
import ec.gob.ambiente.sigma.ejb.entidades.sis.ProjectsGenderInfo;
import ec.gob.ambiente.sigma.ejb.entidades.sis.Sectors;
import ec.gob.ambiente.sigma.ejb.model.RolesUser;
import lombok.Getter;
import lombok.Setter;

@Named
@ViewScoped
public class BuscaProyectosBean implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@Getter
	@Setter
	private boolean esRegistroSalvaguardas;
	
	@Getter
	@Setter //1 implementador  2 estrategico
	private int tipoSocio;
	

	
	@Getter
	@Setter
	private boolean esRegistroGenero;
	
	@Setter
	@Getter
	private boolean datosProyecto;
	
	@Getter
	@Setter
	private Integer codigoLineaGenero;
	
	@Getter
	@Setter
	private Integer codigoProyecto;
	
	@Getter
	@Setter
	private Integer codigoStrategicPartner;
	
	@Getter
	@Setter
	private Integer posicionTab;
	
	@Getter
	@Setter
	private Integer posicionTabGenero;
	
	@Setter
	@Getter
	private int codigoBusquedaProyecto;
	
	@Setter
	@Getter
	private int codigoSocioImplementador;
	
	@Getter
	@Setter
	private List<Partner> listaSociosImplementadores;
	
	@Setter
	@Getter
	private String tituloProyecto;
	
	@Setter
	@Getter
	private String nombreSocioEstrategico;
	
	@Getter
	@Setter
	private List<Project> listaProyectos;
	
	@Getter
	@Setter
	private List<AdvanceExecutionSafeguards> listaProyectosReportados;
	
	@Getter
	@Setter
	private List<Project> listaProyectosSociosReportados;
	
	@Getter
	@Setter
	private List<ProjectsStrategicPartner> listaPartnersProyectos;
	
	@Getter
	@Setter
	private List<Integer> listaSectoresSeleccionados;
	
	@Getter
	@Setter
	private List<Catalogs> listaPreguntasSalvaguardas;
	
	@Getter
	@Setter
	private List<Catalogs> listaLineasAccion;
	
	@Getter
	@Setter
	private List<ProjectQuestions> listaPreguntasAsignadas;
	
	@Getter
	@Setter
	private List<ProjectQuestions> listaSalvaguardasAsignadas;
	
	@Getter
	@Setter
	private List<Integer> preguntasSelecionadas;
	
	@Getter
	@Setter
	private List<Sectors> listaSectoresDisponibles;
	
	@Setter
	@Getter
	private Project proyectoSeleccionado;
	
	@Getter
	@Setter
	private Partner socioImplementador;
	
	@Getter
	@Setter
	private AdvanceExecutionSafeguards advanceExecution;
	
	@Getter
	@Setter
	private Integer anioReporte;
	
	@Getter
	@Setter
	private String periodoDesde;
	
	
	@Getter
	@Setter
	private boolean asignacionSalvaguardas;
	
	@Getter
	@Setter
	private boolean menuAdministrador;
	
	
	@Getter
	@Setter
	private boolean asignacionGenero;
	
	@Getter
	@Setter
	private boolean salvaguardasSociosEstrategicos;
	
	@Getter
	@Setter
	private boolean lineasGeneroSociosEstrategicos;
	
	@Getter
	@Setter
	private boolean sinDatosProyectoPreguntas;
	
	@Getter
	@Setter
	private int tipoRol;
	
//	@Getter
//	@Setter
//	private int codigoRolUsuario;
	
	@Getter
	@Setter
	private RolesUser rolUsuarioSeleccionado;
	
	@Getter
	@Setter
	private List<CatalogTypes> listaLineasGenero;
	
	@Getter
	@Setter
	private boolean proyectoTienePlanGenero;

	@Getter
	@Setter
	private List<ProjectsGenderInfo> listaTemaGenero1;
	
	@Getter
	@Setter
	private List<ProjectsGenderInfo> listaTemaGenero2;
	
	@Getter
	@Setter
	private List<ProjectsGenderInfo> listaTemaGenero3;
	
	@Getter
	@Setter
	private List<ProjectsGenderInfo> listaTemaGeneroOtros;
	
	@Getter
	@Setter
	private List<Indicators> listaIndicadores;
		
//	@Getter
//	@Setter
//	private List<ProjectsSpecificObjectives> listaObjetivosEspecificos;
	
//	@Getter
//	@Setter
//	private ProjectsGenderInfo lineaGeneroSeleccionada;
	
	@Getter
	@Setter
	private boolean nuevoSeguimiento;
	
	@Getter
	@Setter
	private boolean proyectoReportado;
	
	@Getter
	@Setter
	private List<ProjectQuestions> listaPreguntasAsignadasAux;
	
	@Getter
	@Setter
	private List<ProjectsSpecificObjective> listaComponentes;
	
	@Getter
	@Setter
	private List<DtoComponenteSalvaguarda> listaComponentesSalvaguardas;
	
//	@Getter
//	@Setter
//	private int codigoComponente;
	
	@Getter
	@Setter
	private List<Safeguard> listadoSalvaguardasAsignadasProyecto;
	
	@Getter
	@Setter
	private boolean mostrarOpcionesBusqueda;
	
	@Getter
	@Setter
	private boolean mostrarOpcionesBusquedaGenero;
	
//	@Getter
//	@Setter
//	private List<ProjectsSpecificObjectives> listaComponentesParaBusqueda;
	
	@Getter
	@Setter
	private String estadoReporte;
		
	@Getter
	@Setter
	private boolean mostrarTabs;
	
	@Getter
	@Setter
	private boolean nuevaLineaAccion;
	
	@Getter
	@Setter
	private CatalogTypes lineaGeneroSel;
	
	@Getter
	@Setter
	private Catalogs lineaAccionSel;
	
	@Getter
	@Setter
	private List<Component> listadoComponentes;
	
	
	@Getter
	@Setter
	private List<String> listadoComponentesSeleccionados;
	
	@Getter
	@Setter
	private List<ProjectGenderIndicator> listadoProyectoGeneroIndicador;
	
	@Getter
	@Setter
	private ProjectsGenderInfo projectGenderInfoSeleccionado;
	
	@Getter
	@Setter
	private List<ProjectsGenderInfo> listaAccionesdeGeneroProyecto;
	
	@Getter
	@Setter
	private ProjectGenderIndicator indicadorSeleccionado;
		
	@PostConstruct
	public void init(){
//		lineaGeneroSel=new CatalogsType();
//		lineaAccionSel = new Catalogs();
	}
}

