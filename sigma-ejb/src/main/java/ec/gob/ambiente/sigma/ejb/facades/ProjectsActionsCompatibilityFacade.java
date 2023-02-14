package ec.gob.ambiente.sigma.ejb.facades;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import ec.gob.ambiente.sigma.ejb.dao.AbstractFacade;
import ec.gob.ambiente.sigma.ejb.entidades.Action;
import ec.gob.ambiente.sigma.ejb.entidades.Component;
import ec.gob.ambiente.sigma.ejb.entidades.Measure;
import ec.gob.ambiente.sigma.ejb.entidades.ProjectsAction;
import ec.gob.ambiente.sigma.ejb.entidades.ProjectsActionsCompatibility;

/**
 * Session Bean implementation class ProjectsActionsCompatibilityFacade
 */
@Stateless
@LocalBean
public class ProjectsActionsCompatibilityFacade  extends AbstractFacade<ProjectsActionsCompatibility, Integer> {

    /**
     * Default constructor. 
     */
    public ProjectsActionsCompatibilityFacade() {
    	super(ProjectsActionsCompatibility.class, Integer.class);		
    }
	
	public void eliminarLogico(ProjectsActionsCompatibility entidad){
		entidad.setPacoStatus(false);
		edit(entidad);
	}
	
	public void eliminarLogico(Integer pacoId){
		String sql="update sigma.projects_actions_compatibility set paco_status=false where paco_id="+pacoId+";";
		sentenciaNativa(sql);
	}
	
	public List<ProjectsActionsCompatibility> listarCompatibilidadAccionesPorProyecto(Integer proyId) throws Exception{
    	String sql="select c from ProjectsActionsCompatibility c where c.pacoStatus=true and c.pracId.pracStatus=true and c.pracId.psobId.psobStatus=true and c.pracId.psobId.projId.projId=:param1 order by c.pacoId";
		Map<String, Object> camposCondicion=new HashMap<String, Object>();
		camposCondicion.put("param1", proyId);
		return findByCreateQuery(sql, camposCondicion);
    }
	public List<ProjectsActionsCompatibility> listarCompatibilidadAccionesPorProvincia(Integer provId) throws Exception{
    	String sql="select c from ProjectsActionsCompatibility c where c.pacoStatus=true and c.pracId.pracStatus=true and c.pracId.psobId.psobStatus=true and c.pracId.psobId.geloId.geloId=:param1 order by c.pacoId";
		Map<String, Object> camposCondicion=new HashMap<String, Object>();
		camposCondicion.put("param1", provId);
		return findByCreateQuery(sql, camposCondicion);
    }
	
	public void crearRegistroCompatibilidad(Integer pracId, String tipo, Integer idComp) throws Exception{
		ProjectsActionsCompatibility pac=new ProjectsActionsCompatibility();
		pac.setPracId(new ProjectsAction(pracId));
		if(tipo.equals("ACC")){
			pac.setActiId(new Action(idComp));
		}else if(tipo.equals("MED")||tipo.equals("IND")){
			pac.setMeasId(new Measure(idComp));
		}else if(tipo.equals("COM")){
			pac.setCompId(new Component(idComp));
		}
		create(pac);
	}

}
