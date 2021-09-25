package net.mqtts.job.util;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;

import net.mqtts.job.domain.SysJob;

/**
 * 定时任务处理（禁止并发执行）
 * 
 * @author mqtts
 *
 */
@DisallowConcurrentExecution
public class QuartzDisallowConcurrentExecution extends AbstractQuartzJob
{
    @Override
    protected void doExecute(JobExecutionContext context, SysJob sysJob) throws Exception
    {
        JobInvokeUtil.invokeMethod(sysJob);
    }
}
