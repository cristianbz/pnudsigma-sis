package ec.gob.ambiente.sigma.ejb.services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import ec.gob.ambiente.sigma.ejb.dao.AbstractFacade;
import ec.gob.ambiente.sigma.ejb.entidades.User;
import ec.gob.ambiente.sigma.ejb.model.Menu;
import ec.gob.ambiente.sigma.ejb.model.MenuRole;
import ec.gob.ambiente.sigma.ejb.model.Role;
import ec.gob.ambiente.sigma.ejb.model.RolesUser;



@Stateless
public class MenuRoleFacade extends AbstractFacade<MenuRole, Integer> implements Serializable {

	private static final long serialVersionUID = -1397835309701774809L;

	public MenuRoleFacade() {
		super(MenuRole.class, Integer.class);		
	}	
	
	public List<Menu> listarMenusPorRol(List<RolesUser> rolesUsuario) throws Exception{
		List<Role> listaSoloRoles=new ArrayList<Role>();
		for(RolesUser ru:rolesUsuario){
			listaSoloRoles.add(ru.getRole());
		}
		
		String sql=" SELECT DISTINCT mr.menu FROM MenuRole mr WHERE mr.role in :param1 and mr.meroStatus=true and mr.menu.menuStatus=true order by mr.menu.menuEndNode,mr.menu.menuOrder"; 
		Query query = getEntityManager().createQuery(sql);		
		query.setParameter("param1",listaSoloRoles);
         
         @SuppressWarnings("unchecked")
		List<Menu> res = query.getResultList();
         if (res != null && res.size() > 0) {
             return res;
         } else {
             return null;
         }
	}
}