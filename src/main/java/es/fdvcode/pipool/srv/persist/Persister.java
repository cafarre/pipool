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
	
	protected String getBasePath() {
		return FILE_PERSIST_PATH;
	}
	
	protected List<P> loadFromDisc(int numDies) {
		List<P> result = new ArrayList<>();
		if (numDies <= 0) {
			return result;
		}
		
		File curDir = new File(getBasePath() + getEspecificPath());
		if (!curDir.exists() || !curDir.isDirectory()) {
			return result;
		}
		
		File[] files = curDir.listFiles((dir, name) -> 
			name.startsWith(getFilenamePrefix() + "_") && name.endsWith(FILE_PERSIST_EXTENSION)
		);
		
		if (files == null || files.length == 0) {
			return result;
		}
		
		// Ordenem descendent per nom (en format <prefix>_yyyy-MM-dd.dat equival a ordre cronològic del més recent al més antic)
		java.util.Arrays.sort(files, (f1, f2) -> f2.getName().compareTo(f1.getName()));
		
		int filesToLoad = Math.min(numDies, files.length);
		// Carreguem els fitxers en ordre cronològic ascendent (dels més antics als més recents dels seleccionats)
		for (int i = filesToLoad - 1; i >= 0; i--) {
			List<P> list = loadFileFromDisc(files[i]);
			result.addAll(list);
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
		return getBasePath() + getEspecificPath() + getFilenamePrefix() + "_" + sdf.format(date) + FILE_PERSIST_EXTENSION;
	}
	
	private boolean existAnyHistoryFile() {
		File curDir = new File(getBasePath() + getEspecificPath());
		
		if(curDir!=null && curDir.exists()) {
			File[] files = curDir.listFiles((dir, name) ->
				name.startsWith(getFilenamePrefix() + "_") && name.endsWith(FILE_PERSIST_EXTENSION)
			);
			return files != null && files.length > 0;
		}
		return false;
	}

}
