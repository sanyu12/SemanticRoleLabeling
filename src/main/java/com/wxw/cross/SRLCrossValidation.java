package com.wxw.cross;

import java.io.IOException;

import com.wxw.evaluate.SRLEvaluateMonitor;
import com.wxw.evaluate.SRLEvaluator;
import com.wxw.evaluate.SRLMeasure;
import com.wxw.feature.SRLContextGenerator;
import com.wxw.onestep.SRLME;
import com.wxw.onestep.SRLModel;
import com.wxw.stream.SRLSample;
import com.wxw.tree.HeadTreeNode;

import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.eval.CrossValidationPartitioner;

/**
 * 交叉验证
 * @author 王馨苇
 *
 */
public class SRLCrossValidation {
	private final String languageCode;
	private final TrainingParameters params;
	private SRLEvaluateMonitor[] monitor;
	
	/**
	 * 构造
	 * @param languageCode 编码格式
	 * @param params 训练的参数
	 * @param listeners 监听器
	 */
	public SRLCrossValidation(String languageCode,TrainingParameters params,SRLEvaluateMonitor... monitor){
		this.languageCode = languageCode;
		this.params = params;
		this.monitor = monitor;
	}
	
	/**
	 * 交叉验证十折评估
	 * @param file 词性标注的模型文件
	 * @param sample 样本流
	 * @param nFolds 折数
	 * @param contextGenerator 上下文
	 * @throws IOException io异常
	 */
	public void evaluate(ObjectStream<SRLSample<HeadTreeNode>> sample, int nFolds,
			SRLContextGenerator contextGenerator) throws IOException{
		CrossValidationPartitioner<SRLSample<HeadTreeNode>> partitioner = new CrossValidationPartitioner<SRLSample<HeadTreeNode>>(sample, nFolds);
		int run = 1;
		//小于折数的时候
		while(partitioner.hasNext()){
			long start = System.currentTimeMillis();
			System.out.println("Run"+run+"...");
			CrossValidationPartitioner.TrainingSampleStream<SRLSample<HeadTreeNode>> trainingSampleStream = partitioner.next();			
			SRLModel model = SRLME.train(languageCode, trainingSampleStream, params, contextGenerator);
			System.out.println("训练时间："+(System.currentTimeMillis()-start));
			SRLEvaluator evaluator = new SRLEvaluator(new SRLME(model,contextGenerator), monitor);
			SRLMeasure measure = new SRLMeasure();
			
			evaluator.setMeasure(measure);
	        //设置测试集（在测试集上进行评价）
	        evaluator.evaluate(trainingSampleStream.getTestSampleStream());
	        
	        System.out.println(measure);
	        run++;
		}
	}
}