package ec.gob.ambiente.sigma.ejb.facades;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import ec.gob.ambiente.sigma.ejb.dao.AbstractFacade;
import ec.gob.ambiente.sigma.ejb.entidades.ProjectsAction;
import ec.gob.ambiente.sigma.ejb.entidades.ProjectsRisk;
import ec.gob.ambiente.sigma.ejb.entidades.Tracing;
import ec.gob.ambiente.sigma.ejb.entidades.TracingsProgress;

/**
 * Session Bean implementation class TracingsProgressFacade
 */
@Stateless
@LocalBean
public class TracingsProgressFacade extends AbstractFacade<TracingsProgress, Integer> {

	public TracingsProgressFacade() {
		super(TracingsProgress.class, Integer.class);
	}

	public void eliminarLogico(TracingsProgress entidad) {
		entidad.setTrprStatus(false);
		edit(entidad);
	}

	public void crearAccionesPorReporte(Integer tracId, List<Integer> listaAccionesProyecto) throws Exception {
		for (Integer acc : listaAccionesProyecto) {
			TracingsProgress tp = new TracingsProgress();
			tp.setTracId(new Tracing(tracId));
			tp.setPracId(new ProjectsAction(acc));
			create(tp);
		}
	}

	public void crearRiesgosPorReporte(Integer tracId, List<Integer> listaRiesgosProyecto) throws Exception {
		for (Integer rie : listaRiesgosProyecto) {
			TracingsProgress tp = new TracingsProgress();
			tp.setTracId(new Tracing(tracId));
			tp.setPrriId(new ProjectsRisk(rie));
			create(tp);
		}
	}

	public List<TracingsProgress> listarAvanceAccionesPorReporte(Integer tracId) throws Exception {
		String sql = "select t from TracingsProgress t where t.trprStatus=true and t.tracId.tracId=:param1 and t.pracId is not null and t.pracId.pracStatus=true and t.pracId.pracParentId.pracStatus=true order by t.trprId";
		Map<String, Object> camposCondicion = new HashMap<String, Object>();
		camposCondicion.put("param1", tracId);
		return findByCreateQuery(sql, camposCondicion);
	}

	public List<TracingsProgress> listarAvanceRiesgoPorReporte(Integer tracId) throws Exception {
		String sql = "select t from TracingsProgress t where t.trprStatus=true and t.tracId.tracId=:param1 and t.prriId is not null and t.prriId.prriStatus=true order by t.trprId";
		Map<String, Object> camposCondicion = new HashMap<String, Object>();
		camposCondicion.put("param1", tracId);
		return findByCreateQuery(sql, camposCondicion);
	}

	public void guardarListaAvance(List<TracingsProgress> listaAvances) throws Exception {
		for (TracingsProgress t : listaAvances) {
			edit(t);
		}
	}

	public List<TracingsProgress> calcularAvanceDeAcciones(List<TracingsProgress> lst) throws Exception {
		for (TracingsProgress t : lst) {
			Integer progressPercent=0;
			if (t.getTrprReachedValue() != null) {
				progressPercent=(int) Math.round((t.getTrprReachedValue() / t.getPracId().getPracGoalValue()) * 100);
				if(progressPercent>100){
					progressPercent=100;
				}
			}
			t.setTrprProgressPercent(progressPercent);
		}
		return lst;
	}
	
	public int calcularAvanceTotalDeAcciones(List<TracingsProgress> lst) throws Exception {
		int sum1=0;
		for (TracingsProgress t : lst) {
			if(t.getTrprProgressPercent()!=null){
			sum1=sum1+t.getTrprProgressPercent();
			}
		}
		
		return (int) Math.round((sum1 / lst.size()));
	}
	
	
	public List<TracingsProgress> listarAvanceHistoricoPorAccionIndicador(Integer pracId, Integer projId, Integer tracId) throws Exception {
		String sql = "select a from TracingsProgress a, Tracing t, Project p where a.pracId.pracId="+pracId+" and a.trprStatus=true and a.tracId=t and t.tracStatus=true and t.tracRegisterStatus='V' and  t.tracId not in("+tracId+") and t.projId=p and p.projId="+projId+" order by t.tracYear,t.cataId.cataId";
		Map<String, Object> camposCondicion = new HashMap<String, Object>();
		return findByCreateQuery(sql, camposCondicion);
	}
	
	

}
