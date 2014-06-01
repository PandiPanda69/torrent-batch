package fr.thedestiny.torrent.tasklet;

import java.util.List;

import lombok.Setter;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * Simple tasklet that get all torrent currently registered in the queue
 * @author Sébastien
 */
public class ExistingTorrentQueueTasklet implements Tasklet {

	@Setter
	private SessionFactory sessionFactory;
	
	@Setter
	private List<String> currentTorrentQueue;
	
	@SuppressWarnings("unchecked")
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

		Session session = sessionFactory.openSession();
		List<String> result = session.createSQLQuery("SELECT filename FROM TorrentQueue").list();
		session.close();		
		
		currentTorrentQueue.addAll(result);
		
		return RepeatStatus.FINISHED;
	}

}
