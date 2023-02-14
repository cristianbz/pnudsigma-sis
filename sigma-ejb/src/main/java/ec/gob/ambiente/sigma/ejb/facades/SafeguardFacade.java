package ec.gob.ambiente.sigma.ejb.facades;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import ec.gob.ambiente.sigma.ejb.dao.AbstractFacade;
import ec.gob.ambiente.sigma.ejb.entidades.Safeguard;

/**
 * Session Bean implementation class SafeguardFacade
 */
@Stateless
@LocalBean
public class SafeguardFacade extends AbstractFacade<Safeguard, Integer> {

    /**
     * Default constructor. 
     */
    public SafeguardFacade() {
    	super(Safeguard.class, Integer.class);		
    }
	
	public void eliminarLogico(Safeguard entidad){
		entidad.setSafeStatus(false);
		edit(entidad);
	}
	
	public List<Safeguard> listarSalvaguardasPorPlanAccion(Integer paId) throws Exception{
		String sql="select s from Safeguard s where s.safeStatus=true and s.safeLevel=1 and s.acplId.acplId=:param1 order by s.safeOrder ";
		Map<String, Object> camposCondicion=new HashMap<String, Object>();
		camposCondicion.put("param1", paId);
		return findByCreateQuery(sql, camposCondicion);
	}
	
	

}
