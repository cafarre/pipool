package es.fdvcode.pipool.srv.persist;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Persister<P extends Persistible, O> {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	protected static final String FILE_PERSIST_PATH = "data/";
	protected static final String FILE_PERSIST_EXTENSION = ".dat";
		
	public abstract void doPersistencia(Collection<O> listItems);
	
	public abstract List<P> loadHistory(String id, int numDies);
	
	public abstract P newInstance(String linea);
	
	protected abstract String getFilenamePrefix();
	
	protected abstract String getEspecificPath();
	
	protected abstract String getCapsalera();
	
	protected abstract Comparator<P> getComparatorPersistible();
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	protected void doPersistencia(List<P> listToPersist) {
		Collections.sort(listToPersist, getComparatorPersistible());
		this.saveToDisc(listToPersist);
	}
	
	private void saveToDisc(List<P> listToPersist) {
				
		String filename = getResolveFilename(new Date());
		File file = new File(filename);
		
		//Pinta capsalera
		if(!file.exists()) {
			Path carpeta = file.toPath().getParent();
			try {
				Files.createDirectories(carpeta);
			} catch (IOException e) {
				log.error("PERSIST ERROR: NO S'HA POGUT CREAR LA CARPETA {}.", carpeta, e);
				return;
			}
			try(FileWriter fw = new FileWriter(filename, false);
					BufferedWriter bw = new BufferedWriter(fw);
				    PrintWriter out = new PrintWriter(bw)) 
			{
				out.println(this.getCapsalera());
			} 
			catch (IOException e) {
			    log.error("PERSIST ERROR: NO S'HA POGUT CREAR EL FITXER {}.", filename, e);
			    return;
			}
		}
		
		//Afegeix files
		try(FileWriter fw = new FileWriter(filename, true);
			    BufferedWriter bw = new BufferedWriter(fw);
			    PrintWriter out = new PrintWriter(bw)) 
		{
				
			for(Persistible persist : listToPersist) {
			    out.println(persist.marshall());
			}
		} 
		catch (IOException e) {
			log.error("PERSIST ERROR: NO S'HAN POGUT AFEGIR DADES AL FITXER {}.", filename, e);
		}
	}
	
	protected List<P> loadFromDisc(int numDies) {
		List<P> result = new ArrayList<>();
		
		if(existAnyHistoryFile()) {
			int i = 0;
			while(i < 60 && numDies > 0) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DAY_OF_MONTH, -i);
				
				String filename = getResolveFilename(cal.getTime());
				File file = new File(filename);
				if(file.exists()) {
					List<P> list = loadFileFromDisc(file);
					result.addAll(list);
					numDies--;
				}
				
				i++;
			}
		}

		return result;
	}
	
	private List<P> loadFileFromDisc(File file) {
		List<P> result = new ArrayList<>();
		if(file.exists()) {
			try(FileReader fr = new FileReader(file);
				    BufferedReader br = new BufferedReader(fr)) 
			{
				String linea;
				int numLinea=1;
				while((linea = br.readLine())!=null) {
					if(numLinea > 1) { 
						try {
							P pers = newInstance(linea);
							result.add(pers);
						}
						catch(Exception ex) {
							log.error("PERSIST ERROR: NO S'HA POGUT LLEGIR LA LINEA amb contingut {} del fitxer {}.", linea, file.getAbsolutePath(), ex);
						}
					}
					numLinea++;
				}
			} 
			catch (IOException e) {
				log.error("PERSIST ERROR: NO S'HA POGUT LLEGIR EL FITXER {}.", file.getAbsolutePath(), e);
			}
		}
		
		return result;
	}
	
	private String getResolveFilename(Date date) {
		return FILE_PERSIST_PATH + getEspecificPath() + getFilenamePrefix() + "_" + sdf.format(date) + FILE_PERSIST_EXTENSION;
	}
	
	private boolean existAnyHistoryFile() {
		File curDir = new File(FILE_PERSIST_PATH + getEspecificPath());
		
		if(curDir!=null && curDir.exists()) {
			File[] files = curDir.listFiles();
			if(files!=null) {
				
				for (File file: files) {
					if(file.getName().contains(getFilenamePrefix() + "_") && file.getName().contains(FILE_PERSIST_EXTENSION)) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
