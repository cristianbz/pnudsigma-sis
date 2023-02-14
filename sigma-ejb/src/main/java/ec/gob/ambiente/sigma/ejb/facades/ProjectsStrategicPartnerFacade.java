package ec.gob.ambiente.sigma.ejb.facades;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import ec.gob.ambiente.sigma.ejb.dao.AbstractFacade;
import ec.gob.ambiente.sigma.ejb.entidades.Project;
import ec.gob.ambiente.sigma.ejb.entidades.ProjectsStrategicPartner;

/**
 * Session Bean implementation class ProjectStrategicPartnerFacade
 */
@Stateless
@LocalBean
public class ProjectsStrategicPartnerFacade extends  AbstractFacade<ProjectsStrategicPartner, Integer> {

    /**
     * Default constructor. 
     */
    public ProjectsStrategicPartnerFacade() {
    	super(ProjectsStrategicPartner.class, Integer.class);		
    }
	
	public void eliminarLogico(ProjectsStrategicPartner entidad) throws Exception{
		entidad.setPspaStatus(false);
		edit(entidad);
	}
	
	public List<ProjectsStrategicPartner> listaSociosEstrategicosPorProyecto(Integer projId) throws Exception{
		String sql="select sp from ProjectsStrategicPartner sp where sp.pspaStatus=true and sp.projId.projId=:param1 order by sp.pspaType,sp.pspaId";
		Map<String, Object> camposCondicion=new HashMap<String, Object>();
		camposCondicion.put("param1", projId);
		return findByCreateQuery(sql, camposCondicion);
	}
	
	public void eliminarSociosDeProyecto(Integer projId) throws Exception{
		String sql="update sigma.projects_strategic_partners set pspa_status=false where proj_id="+projId+";";
		sentenciaNativa(sql);
	}
	
	public void guardarSociosDeProyecto(List<ProjectsStrategicPartner> lista, Integer projId) throws Exception{
		for(ProjectsStrategicPartner s:lista){
			if(s.getPspaId()!=null&&!s.isSeleccionado()){
				eliminarLogico(s);
			}else if(s.getPspaId()==null&&s.isSeleccionado()){
				s.setProjId(new Project(projId));
				create(s);
			}
		}
	}
	
	public void guardarSociosDeProyectoPrimeraVez(List<ProjectsStrategicPartner> lista, Integer projId) throws Exception{
		for(ProjectsStrategicPartner s:lista){
			if(s.getPspaId()==null){
				s.setProjId(new Project(projId));
				create(s);
			}
		}
	}
	
	public List<String> listarNombresSocioEstrategicosSeleccionados(List<ProjectsStrategicPartner> lista){
		List<String>  res=new ArrayList<String>();
		for(ProjectsStrategicPartner sp:lista){
			if(sp.isSeleccionado()){
				res.add(sp.getPartId().getPartName());
			}
		}
		return res;
	}
}
