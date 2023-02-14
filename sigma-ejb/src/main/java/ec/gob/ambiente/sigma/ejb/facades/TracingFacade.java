package ec.gob.ambiente.sigma.ejb.facades;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import ec.gob.ambiente.sigma.ejb.dao.AbstractFacade;
import ec.gob.ambiente.sigma.ejb.entidades.Tracing;

/**
 * Session Bean implementation class TracingFacade
 */
@Stateless
@LocalBean
public class TracingFacade extends AbstractFacade<Tracing, Integer> {

	public TracingFacade() {
		super(Tracing.class, Integer.class);
	}

	public Tracing crear(String userCreate, Tracing entidad) throws Exception {
		entidad.setTracCreatorUser(userCreate);
		entidad.setTracCreationDate(nowTimespan());
		create(entidad);
		return entidad;
	}

	public Tracing editar(String userUpdate, Tracing entidad) throws Exception {
		entidad.setTracUserUpdate(userUpdate);
		entidad.setTracDateUpdate(nowTimespan());
		edit(entidad);
		return entidad;
	}

	public void eliminarLogico(Tracing entidad) {
		entidad.setTracStatus(false);
		entidad.setTracDateUpdate(nowTimespan());
		edit(entidad);
	}
	
	public List<Tracing> listarReportesPorSocio(Integer partId){
		String sql="select t from Tracing t where t.tracStatus=true and t.projId.partId.partId=:param1 order by t.tracId desc";
		Map<String, Object> camposCondicion=new HashMap<String, Object>();
		camposCondicion.put("param1", partId);
		return findByCreateQuery(sql, camposCondicion);
	}
	
	public List<Tracing> listarReportesPorTituloProyecto(String titulo){
		String sql="select t from Tracing t where t.tracStatus=true and UPPER(t.projId.projTitle) like :param1 order by t.tracId desc";
		Map<String, Object> camposCondicion=new HashMap<String, Object>();
		camposCondicion.put("param1", "%"+titulo.toUpperCase()+"%");
		return findByCreateQuery(sql, camposCondicion);
	}
	
	public List<Tracing> listarReportesPorProyecto(Integer projId){
		String sql="select t from Tracing t where t.tracStatus=true and t.projId.projId=:param1 order by t.tracId desc";
		Map<String, Object> camposCondicion=new HashMap<String, Object>();
		camposCondicion.put("param1",projId);
		return findByCreateQuery(sql, camposCondicion);
	}
	
	public boolean existeReportePorSocioYPeriodo(Integer projId,Integer anio, Integer catId, Integer tracId) throws Exception{
		String sql="select t from Tracing t where t.tracStatus=true and t.projId.projStatus=true and t.projId.projId=:param1 and t.tracYear=:param2 and t.cataId.cataId=:param3 order by t.tracId desc";
		Map<String, Object> camposCondicion=new HashMap<String, Object>();
		camposCondicion.put("param1", projId);
		camposCondicion.put("param2", anio);
		camposCondicion.put("param3", catId);
		List<Tracing> lst=findByCreateQuery(sql, camposCondicion);
		if(lst.isEmpty()){
			return false;
		}else{
			
			if(lst.size()==1&&lst.get(0).getTracId()==tracId){
				return false;
			}else{
				return true;
			}
			
			
		}
		
	}

}
