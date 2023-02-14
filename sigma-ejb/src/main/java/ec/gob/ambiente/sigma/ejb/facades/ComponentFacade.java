package ec.gob.ambiente.sigma.ejb.facades;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;


import ec.gob.ambiente.sigma.ejb.dao.AbstractFacade;
import ec.gob.ambiente.sigma.ejb.entidades.ActionPlan;
import ec.gob.ambiente.sigma.ejb.entidades.Component;
import ec.gob.ambiente.sigma.ejb.entidades.Goal;
import ec.gob.ambiente.sigma.ejb.entidades.SpecificObjective;

/**
 * Session Bean implementation class ComponentFacade
 */
@Stateless
@LocalBean
public class ComponentFacade extends AbstractFacade<Component, Integer>{

    /**
     * Default constructor. 
     */
	public ComponentFacade() {
		super(Component.class, Integer.class);		
	}
	
	public List<Component> listarComponentesPorPlan(Integer acplId) throws Exception{
		String sql="select co from Component co where co.compStatus=true and co.acplId.acplId=:param1 order by co.compCode";
		Map<String, Object> camposCondicion=new HashMap<String, Object>();
		camposCondicion.put("param1", acplId);
		return findByCreateQuery(sql, camposCondicion);
	}
	public void eliminarLogico(Component entidad){
		entidad.setCompStatus(false);
		edit(entidad);
	}
	
	public List<Object[]> listarComponentesResumenPorPlan(Integer acplId) throws Exception{
		String sql="select c.comp_code,c.comp_name,c.comp_id from sigma.components c where c.compStatus=true and c.acpl_id="+acplId+ " order by c.comp_code";
		return consultaNativa(sql);
	}
	
	public Component buscarComponentePAVigentePorCodigo(String codigo) throws Exception{
		String sql="select co from Component co where co.compStatus=true and co.acplId.acplIscurrent=true and co.acplId.acplStatus=true and co.compCode=:param1";
		Map<String, Object> camposCondicion=new HashMap<String, Object>();
		camposCondicion.put("param1", codigo);
		List<Component> res=findByCreateQuery(sql, camposCondicion);
		if(res.isEmpty()){
			return null;
		}else{
			return res.get(0);
		}
		
	}

}
