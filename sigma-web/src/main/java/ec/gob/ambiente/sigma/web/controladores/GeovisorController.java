 package ec.gob.ambiente.sigma.web.controladores;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.primefaces.PrimeFaces;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ec.gob.ambiente.sigma.ejb.entidades.Catalog;
import ec.gob.ambiente.sigma.ejb.entidades.Project;
import ec.gob.ambiente.sigma.ejb.entidades.ProjectsGeographicalArea;
import ec.gob.ambiente.sigma.ejb.entidades.Safeguard;
import ec.gob.ambiente.sigma.ejb.facades.GeoFacade;
import ec.gob.ambiente.sigma.ejb.facades.ProjectFacade;
import ec.gob.ambiente.sigma.ejb.facades.ProjectsGeographicalAreaFacade;
import ec.gob.ambiente.sigma.ejb.facades.SafeguardFacade;
import ec.gob.ambiente.sigma.web.utils.TipoRetornoAProyecto;

/**
 * @author proamazonia
 *
 */
@Named("geo")
@ViewScoped
public class GeovisorController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(GeovisorController.class.getName());

	
	private List<String> listMetadataNombreCapas;
	private List<List<String>> listMetadataDescripcionFeatures;
	private List<List<String>> listDataGeoJson;
	private Project proyectoActual;
	private List<ProjectsGeographicalArea> listaTablasProyectos;
	private String dummyVar;
	private int tipoGeoVisor;
	
	private String tipoBusqCapa;
	private ProjectsGeographicalArea capaProyecto;
	private Catalog capaBase;
	private List<Safeguard> listaSalvaguardas;
	private Safeguard capaSalvaguarda;
	
	
	private List<Capa> listaCapas;

	@Inject
	private SesionControlador sesBean;
	@Inject
	private AplicacionControlador appBean;

	@EJB
	private GeoFacade geoFacade;
	@EJB
	private ProjectFacade proyFacade;
	@EJB
	private ProjectsGeographicalAreaFacade proyGeoAreaFacade;
	@EJB
	private SafeguardFacade salvaguardaFacade;

	public GeovisorController() {
		// TODO Auto-generated constructor stub
	}

	@PostConstruct
	public void inicializar() {
		try {
			dummyVar = "";
			limpiarObjetos();
			
			HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext()
					.getSession(false);
			String attr = (String) session.getAttribute("TIPO_GEOVISOR");
			session.setAttribute("TIPO_GEOVISOR", null);
			listaTablasProyectos = proyGeoAreaFacade.listarTablasProyectos();
			if (attr != null && attr.contains("SOLO_PROYECTO")) {
				tipoGeoVisor = 1;
				Integer projId = (Integer) session.getAttribute("ID_PROYECTO_ACTUAL");
				proyectoActual = new Project();
				
				if (projId != null) {
					proyectoActual=proyFacade.find(projId);
				}
				PrimeFaces.current().executeScript("inicializarMapa();");
				
				
			} else {
				//ingreso al geovisor público
				PrimeFaces.current().executeScript("inicializarMapa();");
				PrimeFaces.current().executeScript("cargarMapa();");
				listaSalvaguardas=salvaguardaFacade.listarSalvaguardasPorPlanAccion(1);
			}

			

		} catch (Exception ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
	}
	
	private void limpiarObjetos(){
		listaTablasProyectos= new ArrayList<>();
		listaSalvaguardas=new ArrayList<>();
		listMetadataNombreCapas = new ArrayList<>();
		listMetadataDescripcionFeatures = new ArrayList<>();
		listDataGeoJson = new ArrayList<>();
		capaProyecto=new ProjectsGeographicalArea();
		capaSalvaguarda=new Safeguard();
		capaBase=new Catalog();
		listaCapas=new ArrayList<>();
	}
	
	public void cargarInformacionProyecto(){
		try{
			if(proyectoActual.getProjId()!=null){
				
				for(ProjectsGeographicalArea p : listaTablasProyectos){
					if(p.getProjId().getProjId()==proyectoActual.getProjId()){
						procesarCapaProyecto(p);
					}
				}

			}
			
		}catch (Exception ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
	}

	public void cargarInformacionProyectos() {
		try {

			for (ProjectsGeographicalArea p : listaTablasProyectos) {

				procesarCapaProyecto(p);

			}
		} catch (Exception ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
	}
	
	public void cargarInformacionCapasBase() {
		try {
			for(Catalog c:appBean.getLstCatCapasGeoBase()){
				if(!c.getCataText1().contains(";SNMB")){
					procesarCapaBase(c);
				}
				
			}
		} catch (Exception ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
	}

	public void volverARegistroProyecto() {
       limpiarObjetos();
		sesBean.redireccionarAPaginaConParametros("projects", "RegistroProyecto", "REDIRECCION_DESDE",
				TipoRetornoAProyecto.DESDE_GEOVISOR.getCodigo(), "ID_PROYECTO_ACTUAL", proyectoActual.getProjId());
	}

	
	public void mostrarDialogoCapas(){
		try{
			PrimeFaces.current().executeScript("PF('dlgCapas').show()");
		}catch (Exception ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
	}
	
	public void aniadirCapaProyecto(){
		try{
			procesarCapaProyecto(capaProyecto);
			capaProyecto=new ProjectsGeographicalArea();
		}catch (Exception ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
	}
	
	private void procesarCapaProyecto(ProjectsGeographicalArea capa){
		try{
			Gson utilGson = new Gson();
			capa=obtenerObjetoCapaProyecto(capa.getPgarId());
			
			List<Object[]> lst2 = geoFacade.listarDescGeomAsGeoJson(capa.getPgarGeoTable(),capa.getPgarGeoTableCols());
			List<String> geoData=new ArrayList<>();
			List<String> descFeat=new ArrayList<>();
			for(Object[] o:lst2){
				geoData.add(String.valueOf(o[1]));
				String d=capa.getProjId().getProjShortName()+"<br/><br/>"+String.valueOf(o[0]);
				descFeat.add(utilGson.toJson(d));
			}
			listDataGeoJson.add(geoData);
			listMetadataDescripcionFeatures.add(descFeat);
			JsonObject nombreCapa=new JsonObject();
			nombreCapa.addProperty("nombre", proyFacade.obtenerMetadataSimpleProyecto(capa.getProjId(),capa.getCataId()));
			if(capa.getCataId()!=null&&capa.getCataId().getCataText2()!=null){
				if(capa.getCataId().getCataText2().startsWith("#")){
					nombreCapa.addProperty("icono","");
					nombreCapa.addProperty("color",capa.getCataId().getCataText2());
				}else{
					nombreCapa.addProperty("icono",capa.getCataId().getCataText2());
					nombreCapa.addProperty("color","#FFFFFF");
				}
				
			}else{
				nombreCapa.addProperty("icono","");
				nombreCapa.addProperty("color","#FFFFFF");
			}
			
			
			//String nombreCapa=utilGson.toJson(capa.getPgarGeoType()+" "+proyFacade.obtenerMetadataSimpleProyecto(capa.getProjId())+" - "+appBean.textoEjeTematico(capa.getPgarGeoType()));
			//System.out.println(nombreCapa);
			listMetadataNombreCapas.add(utilGson.toJson(nombreCapa));
			Capa c=new Capa();
			c.setDescripcionCapa(nombreCapa.get("nombre").getAsString());
			c.setNumFeatures(lst2.size());
			c.setTipo(obtenerTipoGeometria(geoData.get(0)));
			c.setCategoria("Proyecto");
			c.setFuente("SIGMA");
			c.setColor(nombreCapa.get("color").getAsString());
			c.setIcono(nombreCapa.get("icono").getAsString());
			System.out.println(nombreCapa.get("icono").getAsString());
			listaCapas.add(c);
		}catch (Exception ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
	}
	
	private ProjectsGeographicalArea obtenerObjetoCapaProyecto(Integer id){
		ProjectsGeographicalArea res=new ProjectsGeographicalArea();
		try{
			for(ProjectsGeographicalArea c:listaTablasProyectos){
				if(c.getPgarId().equals(id)){
					res=c;
					break;
				}
			}
		}catch (Exception ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
		return res;
	}
	
	public void aniadirCapaSalvaguarda(){
		try{
			procesarCapaSalvaguarda(capaSalvaguarda);
			capaSalvaguarda=new Safeguard();
		}catch (Exception ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
	}
	
	private void procesarCapaSalvaguarda(Safeguard capa){
		try{
			Gson utilGson = new Gson();
			capa=obtenerObjetoCapaSalvaguarda(capa.getSafeId());
			
			List<Object[]> lst2 = geoFacade.listarCantGeomAsGeoJsonPorSalvaguarda(capa.getSafeCode());
      		if(!lst2.isEmpty()){
      			List<String> geoData=new ArrayList<>();
    			List<String> descFeat=new ArrayList<>();
    			for(Object[] o:lst2){
    				geoData.add(String.valueOf(o[1]));
    				descFeat.add(utilGson.toJson(String.valueOf(o[0])));
    				
    			}
    			listDataGeoJson.add(geoData);
    			listMetadataDescripcionFeatures.add(descFeat);
    			//String nombreCapa=utilGson.toJson("SALVAGUARDA "+capa.getSafeCode());
    			JsonObject nombreCapa=new JsonObject();
    			nombreCapa.addProperty("nombre", "SALVAGUARDA "+capa.getSafeCode());
    			nombreCapa.addProperty("icono",capa.getSafeCode().toLowerCase());
    			listMetadataNombreCapas.add(utilGson.toJson(nombreCapa));
    			Capa c=new Capa();
    			c.setDescripcionCapa(nombreCapa.get("nombre").getAsString());
    			c.setNumFeatures(lst2.size());
    			c.setCategoria("Salvaguarda");
    			c.setFuente("SIS");
    			c.setTipo(obtenerTipoGeometria(geoData.get(0)));
    			c.setIcono(capa.getSafeCode().toLowerCase());
    			listaCapas.add(c);
      		}else{
      			sesBean.addErrorMessage(appBean.getBundle().getString("msg143"));
      		}
			
		}catch (Exception ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
	}
	
	private Safeguard obtenerObjetoCapaSalvaguarda(Integer id){
		Safeguard res=new Safeguard();
		try{
			for(Safeguard c:listaSalvaguardas){
				if(c.getSafeId().equals(id)){
					res=c;
					break;
				}
			}
		}catch (Exception ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
		return res;
	}
	
	public void aniadirCapaBase(){
		try{
			procesarCapaBase(capaBase);
			capaBase=new Catalog();
		}catch (Exception ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
	}
	
	private void procesarCapaBase(Catalog capa){
		try{
			Gson utilGson = new Gson();
			capa=obtenerObjetoCatalogoPorId(capa.getCataId());
			String color="#FFF000";
			color=capa.getCataText2().split(";")[2];
			int idx=0;
			for(int i=0;i<appBean.getLstCatCapasGeoBase().size();i++){
				if(capa.getCataId().equals(appBean.getLstCatCapasGeoBase().get(i).getCataId())){
					idx=i;
				}
			}
			
			List<Object[]> lst2 = appBean.getListaGeoCapasBase().get(idx);
			List<String> geoData=new ArrayList<>();
			List<String> descFeat=new ArrayList<>();
			int t=0;
			for(Object[] o:lst2){
				t++;
				geoData.add(String.valueOf(o[1]));
				descFeat.add(utilGson.toJson(String.valueOf(o[0])));
				if(t%1000==0){
					Thread.sleep(150);
				}
			}
			listDataGeoJson.add(geoData);
			listMetadataDescripcionFeatures.add(descFeat);
			JsonObject nombreCapa=new JsonObject();
			String[] d=capa.getCataText1().split(";");
			nombreCapa.addProperty("nombre", d[0]);
			nombreCapa.addProperty("icono","");
			nombreCapa.addProperty("color",color);
			listMetadataNombreCapas.add(utilGson.toJson(nombreCapa));
			
			Capa c=new Capa();
			c.setDescripcionCapa(nombreCapa.get("nombre").getAsString());
			c.setNumFeatures(lst2.size());
			c.setTipo(obtenerTipoGeometria(geoData.get(0)));
			c.setCategoria("Capa Base");
			c.setColor(nombreCapa.get("color").getAsString());
			c.setIcono("");
			if(d.length>1){
				c.setFuente(d[1]);
			}
			listaCapas.add(c);
			
		}catch (Exception ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
	}
	
	private Catalog obtenerObjetoCatalogoPorId(Integer id){
		Catalog res=new Catalog();
		try{
			for(Catalog c:appBean.getLstCatCapasGeoBase()){
				if(c.getCataId().equals(id)){
					res=c;
					break;
				}
			}
		}catch (Exception ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
		return res;
	}
	
	public void eliminarCapaSeleccionada(int idx){
		try{
			listaCapas.remove(idx);
			listMetadataNombreCapas.remove(idx);
			listDataGeoJson.remove(idx);
			listMetadataDescripcionFeatures.remove(idx);
		}catch (Exception ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
	}
	
	public void subirPosicionItemLista(int idxActual){
		try{
			if(idxActual>0){
				reordenarListaCapas(idxActual, true);
			}
		}catch (Exception ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
	}
	
	private void reordenarListaCapas(int idxAOrdenar,boolean arriba){
		
		try{
			Capa c1=listaCapas.get(idxAOrdenar);
			Capa c2=listaCapas.get(idxAOrdenar-1);
			List<String> g1=listDataGeoJson.get(idxAOrdenar);
			List<String> g2=listDataGeoJson.get(idxAOrdenar-1);
			List<String> f1=listMetadataDescripcionFeatures.get(idxAOrdenar);
			List<String> f2=listMetadataDescripcionFeatures.get(idxAOrdenar-1);
			String n1=listMetadataNombreCapas.get(idxAOrdenar);
			String n2=listMetadataNombreCapas.get(idxAOrdenar-1);
			List<Capa> lstPosterior=new ArrayList<>();
			List<List<String>> lstGPosterior=new ArrayList<>();
			List<List<String>> lstFPosterior=new ArrayList<>();
			List<String> lstNPosterior=new ArrayList<>();
			int numCapas=listaCapas.size();
			for(int a=(idxAOrdenar+1);a<numCapas;a++){
				lstPosterior.add(listaCapas.get(a));
				lstGPosterior.add(listDataGeoJson.get(a));
				lstFPosterior.add(listMetadataDescripcionFeatures.get(a));
				lstNPosterior.add(listMetadataNombreCapas.get(a));
			}
			int itemsAEliminar=(numCapas-(idxAOrdenar+1))+2;
			for(int b=1;b<=itemsAEliminar;b++){
				listaCapas.remove(listaCapas.size()-1);
				listDataGeoJson.remove(listDataGeoJson.size()-1);
				listMetadataDescripcionFeatures.remove(listMetadataDescripcionFeatures.size()-1);
				listMetadataNombreCapas.remove(listMetadataNombreCapas.size()-1);
			}
			listaCapas.add(c1);
			listaCapas.add(c2);
			listaCapas.addAll(lstPosterior);
			listDataGeoJson.add(g1);
			listDataGeoJson.add(g2);
			listDataGeoJson.addAll(lstGPosterior);
			listMetadataDescripcionFeatures.add(f1);
			listMetadataDescripcionFeatures.add(f2);
			listMetadataDescripcionFeatures.addAll(lstFPosterior);
			listMetadataNombreCapas.add(n1);
			listMetadataNombreCapas.add(n2);
			listMetadataNombreCapas.addAll(lstNPosterior);
			
		}catch (Exception ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
	}
	
	public void visualizarCapasEnMapa(){
		try{
			//cargarObjetos
			/*int i=0;
			for(Capa c:listaCapas){
				if(!c.categoria.equals("Salvaguarda")){
					c.setIcono(obtenerIconoCapa(c, i));
				}else{
					c.setIcono(c.descripcionCapa.substring(c.descripcionCapa.length()-1));
				}
				i++;
			}*/
			
		}catch (Exception ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
	}
	
	private String obtenerTipoGeometria(String textoAnalizar){
		String tipo="";
		try{
			if(textoAnalizar.contains("Polygon")){
				tipo="fa-square";
			}else{
				if(textoAnalizar.contains("Point")){
					tipo="fa-circle";
				}else{
					tipo="fa-underline";
				}
			}
		}catch (Exception ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
		return tipo;
	}
	
	private String obtenerIconoCapa(Capa capa, int idx){
		String res="";
		try{
			
				if(capa.getTipo().equals("fa-circle")){
					//res="<div style='width:5px;height:5px;background:#5298DF;'></div>";
					res="";
				}else{
					if(capa.getTipo().equals("fa-square")){
						res="<div style='border:1px;width:10px;height:15px;background:"+obtenerColor(idx)+";'></div>";
					}else{
						res="<div style='border:1px;width:10px;height:2px;background:"+obtenerColor(idx)+";'></div>";
					}
				}
			
			
			
		}catch (Exception ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
		return res;
	}
	
	private String obtenerColor(int idx){
		return idx < 1 ? "#fc8d59" :
	           idx < 2  ? "#DBDB42" :
	           idx < 3  ? "#91cf60" :
	           idx < 4  ? "#FC4E2A" :
	           idx < 5   ? "#FD8D3C" :
	           idx < 6   ? "#FEB24C" :
	           idx < 7   ? "#FED976" :
	                      "#FFEDA0";
	}
	
	public String getDummyVar() {
		return dummyVar;
	}

	public void setDummyVar(String dummyVar) {
		this.dummyVar = dummyVar;
	}

	public List<List<String>> getListDataGeoJson() {
		return listDataGeoJson;
	}

	public void setListDataGeoJson(List<List<String>> listDataGeoJson) {
		this.listDataGeoJson = listDataGeoJson;
	}

	public Project getProyectoActual() {
		return proyectoActual;
	}

	public void setProyectoActual(Project proyectoActual) {
		this.proyectoActual = proyectoActual;
	}

	

	public List<String> getListMetadataNombreCapas() {
		return listMetadataNombreCapas;
	}

	public void setListMetadataNombreCapas(List<String> listMetadataNombreCapas) {
		this.listMetadataNombreCapas = listMetadataNombreCapas;
	}

	public List<List<String>> getListMetadataDescripcionFeatures() {
		return listMetadataDescripcionFeatures;
	}

	public void setListMetadataDescripcionFeatures(List<List<String>> listMetadataDescripcionFeatures) {
		this.listMetadataDescripcionFeatures = listMetadataDescripcionFeatures;
	}

	public int getTipoGeoVisor() {
		return tipoGeoVisor;
	}

	public void setTipoGeoVisor(int tipoGeoVisor) {
		this.tipoGeoVisor = tipoGeoVisor;
	}
	
	
	public void pruebaWFS(){
		try{
			String sURL = "http://ide.ambiente.gob.ec/geoserver/mae_ide/ows?service=WFS&version=1.0.0&format_options=CHARSET:UTF-8&request=GetFeature&typeName=mae_ide:v_fc010_deforestacion_16_18_a&outputFormat=application/json"; //just a string
			
			//String sURL = "http://maps.googleapis.com/maps/api/geocode/json?address=google"; //just a string

		    // Connect to the URL using java's native library
		    URL url = new URL(sURL);
		    URLConnection request = url.openConnection();
		    request.connect();
		    System.out.println("265");		
		    //InputStreamReader isr=new InputStreamReader((InputStream) request.getContent());
		    InputStream is=(InputStream)request.getContent();
		    System.out.println("268");	
		    BufferedInputStream bi = new BufferedInputStream(is);
		    System.out.println("270");	
		    int i;
		    while((i=bi.read()) != -1) {
		        System.out.print((char)i);
		      }
		    System.out.println("275");	
		      is.close();
		      bi.close();
		    // Convert to a JSON object to print data
		   // JsonParser jp = new JsonParsedescripcionCapar(); //from gson
		   //// JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
		   /// JsonObject rootobj = root.getAsJsonObject(); //May be an array, may be an object. 
		   // System.out.println(rootobj.size());
		   // String zipcode = rootobj.get("zip_code").getAsString(); 
		}catch(Exception ex){
			System.out.println(ex);
		}
	}

	

	public List<Capa> getListaCapas() {
		return listaCapas;
	}

	public void setListaCapas(List<Capa> listaCapas) {
		this.listaCapas = listaCapas;
	}
	
	


	public String getTipoBusqCapa() {
		return tipoBusqCapa;
	}

	public void setTipoBusqCapa(String tipoBusqCapa) {
		this.tipoBusqCapa = tipoBusqCapa;
	}




	public ProjectsGeographicalArea getCapaProyecto() {
		return capaProyecto;
	}

	public void setCapaProyecto(ProjectsGeographicalArea capaProyecto) {
		this.capaProyecto = capaProyecto;
	}




	public List<ProjectsGeographicalArea> getListaTablasProyectos() {
		return listaTablasProyectos;
	}

	public void setListaTablasProyectos(List<ProjectsGeographicalArea> listaTablasProyectos) {
		this.listaTablasProyectos = listaTablasProyectos;
	}

	


	public Catalog getCapaBase() {
		return capaBase;
	}

	public void setCapaBase(Catalog capaBase) {
		this.capaBase = capaBase;
	}




	public List<Safeguard> getListaSalvaguardas() {
		return listaSalvaguardas;
	}

	public void setListaSalvaguardas(List<Safeguard> listaSalvaguardas) {
		this.listaSalvaguardas = listaSalvaguardas;
	}

	public Safeguard getCapaSalvaguarda() {
		return capaSalvaguarda;
	}

	public void setCapaSalvaguarda(Safeguard capaSalvaguarda) {
		this.capaSalvaguarda = capaSalvaguarda;
	}




	public class Capa {
		  int idx;
		  String descripcionCapa;
		  int numFeatures;
		  String tipo;
		  String icono;
		  String categoria;
		  String fuente;
		  String color;
		  
		  Capa(){
			  descripcionCapa="";
		  }

		public int getIdx() {
			return idx;
		}

		public void setIdx(int idx) {
			this.idx = idx;
		}

		public String getDescripcionCapa() {
			return descripcionCapa;
		}

		public void setDescripcionCapa(String descripcionCapa) {
			this.descripcionCapa = descripcionCapa;
		}

		public int getNumFeatures() {
			return numFeatures;
		}

		public void setNumFeatures(int numFeatures) {
			this.numFeatures = numFeatures;
		}

		public String getTipo() {
			return tipo;
		}

		public void setTipo(String tipo) {
			this.tipo = tipo;
		}

		public String getIcono() {
			return icono;
		}

		public void setIcono(String icono) {
			this.icono = icono;
		}

		public String getCategoria() {
			return categoria;
		}

		public void setCategoria(String categoria) {
			this.categoria = categoria;
		}
		  
		

		public String getColor() {
			return color;
		}

		public void setColor(String color) {
			this.color = color;
		}

		public String getFuente() {
			return fuente;
		}

		public void setFuente(String fuente) {
			this.fuente = fuente;
		}
		
		
		  
		  
	}

}
