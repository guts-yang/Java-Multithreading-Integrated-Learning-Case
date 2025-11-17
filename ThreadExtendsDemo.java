/**
 * ThreadExtendsDemo - 演示通过继承Thread类创建线程
 * 
 * 本类展示了Java多线程编程的第一种创建方式：继承Thread类
 * 包含完整的理论说明和实际应用场景
 * 
 * @author Java Learning Tutorial
 * @version 1.0
 * @date 2024
 */

public class ThreadExtendsDemo {
    
    /**
     * 自定义线程类 - 继承Thread类
     * 这是创建线程最直接的方式
     */
    static class CalculatorThread extends Thread {
        private String threadName;
        private int start;
        private int end;
        
        /**
         * 构造函数 - 初始化线程参数
         * @param name 线程名称，用于区分不同线程
         * @param start 计算起始值
         * @param end 计算结束值
         */
        public CalculatorThread(String name, int start, int end) {
            this.threadName = name;
            this.start = start;
            this.end = end;
            // 设置线程名称，便于调试和识别
            setName(name);
        }
        
        /**
         * 重写run()方法 - 线程执行的具体逻辑
         * 这是线程的核心执行体，包含线程要完成的任务
         */
        @Override
        public void run() {
            System.out.println("🚀 线程 " + threadName + " 开始执行");
            System.out.println("📊 " + threadName + " 计算范围: " + start + " 到 " + end);
            
            long sum = 0; // 用于累加计算结果
            
            // 执行计算任务
            for (int i = start; i <= end; i++) {
                // 模拟计算过程，增加输出便于观察
                sum += i;
                
                // 每1000次迭代输出一次进度
                if (i % 1000 == 0) {
                    System.out.println("🔄 " + threadName + " 当前进度: " + i + ", 累加和: " + sum);
                    
                    // 模拟CPU密集型计算，添加短暂休眠
                    try {
                        Thread.sleep(50); // 休眠50毫秒
                    } catch (InterruptedException e) {
                        System.err.println("❌ " + threadName + " 被中断");
                        break;
                    }
                }
            }
            
            // 输出最终结果
            System.out.println("✅ " + threadName + " 执行完毕");
            System.out.println("📈 " + threadName + " 最终结果: " + sum);
            System.out.println("🏁 线程 " + threadName + " 生命周期结束");
        }
        
        /**
         * 获取线程信息
         * @return 线程信息字符串
         */
        public String getThreadInfo() {
            return String.format("线程名: %s, 状态: %s, 计算范围: %d-%d", 
                               getName(), getState(), start, end);
        }
    }
    
    /**
     * 文件处理线程类 - 展示实际应用场景
     * 模拟文件批量处理业务
     */
    static class FileProcessorThread extends Thread {
        private String[] fileNames;
        private int threadId;
        
        /**
         * 文件处理器构造函数
         * @param threadId 线程ID
         * @param fileNames 需要处理的文件名列表
         */
        public FileProcessorThread(int threadId, String[] fileNames) {
            this.threadId = threadId;
            this.fileNames = fileNames;
            setName("文件处理线程-" + threadId);
        }
        
        @Override
        public void run() {
            System.out.println("📁 " + getName() + " 开始处理文件列表");
            System.out.println("🔍 需要处理 " + fileNames.length + " 个文件");
            
            for (int i = 0; i < fileNames.length; i++) {
                String fileName = fileNames[i];
                
                // 模拟文件处理过程
                System.out.println("📄 " + getName() + " 正在处理文件: " + fileName);
                
                // 模拟文件处理时间
                try {
                    Thread.sleep(200 + (int)(Math.random() * 300)); // 随机休眠200-500ms
                } catch (InterruptedException e) {
                    System.err.println("❌ " + getName() + " 处理被中断");
                    break;
                }
                
                // 模拟处理结果
                String result = "处理完成: " + fileName;
                System.out.println("✅ " + getName() + " - " + result);
                
                // 显示进度
                int progress = (i + 1) * 100 / fileNames.length;
                System.out.println("📊 " + getName() + " 进度: " + progress + "%");
            }
            
            System.out.println("🎯 " + getName() + " 任务完成！");
        }
    }
    
    /**
     * 字符串重复方法 - 兼容Java 8
     */
    private static String repeat(String str, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    /**
     * 字符串填充方法 - 兼容Java 8
     */
    private static String padEnd(String str, int totalLength, String padStr) {
        if (str.length() >= totalLength) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() < totalLength) {
            sb.append(padStr);
        }
        return sb.toString();
    }

    /**
     * 主方法 - 演示线程创建和执行过程
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        System.out.println(repeat("=", 60));
        System.out.println("🎓 ThreadExtendsDemo - 继承Thread类创建线程演示");
        System.out.println(repeat("=", 60));
        
        // 展示1: 基本线程创建和执行
        demonstrateBasicThreadCreation();
        
        // 展示2: 多线程并发执行
        demonstrateConcurrentExecution();
        
        // 展示3: 实际应用场景 - 文件处理
        demonstrateFileProcessing();
        
        // 展示4: 线程生命周期观察
        demonstrateThreadLifecycle();
    }
    
    /**
     * 演示1: 基本线程创建和执行
     * 展示如何继承Thread类创建自定义线程
     */
    private static void demonstrateBasicThreadCreation() {
        System.out.println("\n" + padEnd("🔸 演示1: 基本线程创建和执行", 50, " "));
        System.out.println(repeat("-", 50));
        
        // 创建线程实例
        CalculatorThread thread1 = new CalculatorThread("计算器-1", 1, 5000);
        CalculatorThread thread2 = new CalculatorThread("计算器-2", 5001, 10000);
        
        // 显示线程创建后的状态信息
        System.out.println("📋 线程创建完成，状态信息：");
        System.out.println("  " + thread1.getThreadInfo());
        System.out.println("  " + thread2.getThreadInfo());
        
        // 启动线程 - 调用start()方法而不是run()方法
        System.out.println("\n🚀 启动线程...");
        thread1.start();
        thread2.start();
        
        // 等待线程执行完毕
        try {
            thread1.join(); // 等待thread1执行完毕
            thread2.join(); // 等待thread2执行完毕
            System.out.println("\n✅ 所有计算线程执行完毕");
        } catch (InterruptedException e) {
            System.err.println("❌ 主线程被中断");
        }
    }
    
    /**
     * 演示2: 多线程并发执行
     * 展示多个线程同时执行，提高任务处理效率
     */
    private static void demonstrateConcurrentExecution() {
        System.out.println("\n" + padEnd("🔸 演示2: 多线程并发执行", 50, " "));
        System.out.println(repeat("-", 50));
        
        // 创建多个计算线程，每个处理不同的数据范围
        System.out.println("📊 创建4个计算线程并发处理不同数据范围");
        
        CalculatorThread[] threads = new CalculatorThread[4];
        for (int i = 0; i < 4; i++) {
            int start = i * 2500 + 1;
            int end = (i + 1) * 2500;
            threads[i] = new CalculatorThread("并发计算-" + (i + 1), start, end);
        }
        
        // 显示所有线程信息
        System.out.println("\n📋 线程信息汇总：");
        for (CalculatorThread thread : threads) {
            System.out.println("  " + thread.getThreadInfo());
        }
        
        // 同时启动所有线程
        System.out.println("\n🚀 启动所有并发线程...");
        for (CalculatorThread thread : threads) {
            thread.start();
        }
        
        // 等待所有线程完成
        try {
            for (CalculatorThread thread : threads) {
                thread.join(); // 等待每个线程完成
            }
            System.out.println("\n✅ 所有并发计算线程执行完毕");
        } catch (InterruptedException e) {
            System.err.println("❌ 主线程被中断");
        }
    }
    
    /**
     * 演示3: 实际应用场景 - 文件处理
     * 展示继承Thread类在实际业务中的应用
     */
    private static void demonstrateFileProcessing() {
        System.out.println("\n" + padEnd("🔸 演示3: 文件处理应用场景", 50, " "));
        System.out.println(repeat("-", 50));
        
        // 模拟文件列表
        String[] files1 = {"data1.txt", "data2.txt", "data3.txt", "data4.txt", "data5.txt"};
        String[] files2 = {"report1.docx", "report2.docx", "report3.docx", "report4.docx"};
        
        // 创建文件处理线程
        FileProcessorThread processor1 = new FileProcessorThread(1, files1);
        FileProcessorThread processor2 = new FileProcessorThread(2, files2);
        
        System.out.println("📁 启动文件处理任务");
        System.out.println("  处理线程1: " + files1.length + " 个文件");
        System.out.println("  处理线程2: " + files2.length + " 个文件");
        
        // 启动文件处理线程
        processor1.start();
        processor2.start();
        
        // 等待处理完成
        try {
            processor1.join();
            processor2.join();
            System.out.println("\n✅ 所有文件处理任务完成");
        } catch (InterruptedException e) {
            System.err.println("❌ 文件处理被中断");
        }
    }
    
    /**
     * 演示4: 线程生命周期观察
     * 展示线程在不同状态下的行为
     */
    private static void demonstrateThreadLifecycle() {
        System.out.println("\n" + padEnd("🔸 演示4: 线程生命周期观察", 50, " "));
        System.out.println(repeat("-", 50));
        
        // 创建线程但不启动
        CalculatorThread lifecycleThread = new CalculatorThread("生命周期观察", 1, 100);
        
        System.out.println("📋 线程状态观察：");
        System.out.println("  1. 线程创建后状态: " + lifecycleThread.getState());
        
        // 启动线程
        lifecycleThread.start();
        
        System.out.println("  2. 线程启动后状态: " + lifecycleThread.getState());
        
        // 监控线程状态变化
        Thread monitorThread = new Thread(() -> {
            while (lifecycleThread.isAlive()) {
                System.out.println("  🔄 监控: " + lifecycleThread.getName() + 
                                 " 当前状态: " + lifecycleThread.getState());
                try {
                    Thread.sleep(200); // 每200ms检查一次
                } catch (InterruptedException e) {
                    break;
                }
            }
            System.out.println("  ✅ 线程已终止，最终状态: " + lifecycleThread.getState());
        });
        
        monitorThread.start();
        
        // 等待线程完成
        try {
            lifecycleThread.join();
            monitorThread.join();
            System.out.println("\n✅ 线程生命周期观察完成");
        } catch (InterruptedException e) {
            System.err.println("❌ 生命周期观察被中断");
        }
    }
    
    /**
     * 演示5: 线程优先级和名称设置
     * 展示如何控制线程的执行优先级和命名
     */
    private static void demonstrateThreadProperties() {
        System.out.println("\n" + padEnd("🔸 演示5: 线程属性控制", 50, " "));
        System.out.println(repeat("-", 50));
        
        // 创建具有不同优先级的线程
        CalculatorThread lowPriority = new CalculatorThread("低优先级线程", 1, 1000);
        CalculatorThread normalPriority = new CalculatorThread("普通优先级线程", 1001, 2000);
        CalculatorThread highPriority = new CalculatorThread("高优先级线程", 2001, 3000);
        
        // 设置线程优先级（1-10，5为默认）
        lowPriority.setPriority(Thread.MIN_PRIORITY);      // 1
        normalPriority.setPriority(Thread.NORM_PRIORITY);  // 5  
        highPriority.setPriority(Thread.MAX_PRIORITY);     // 10
        
        System.out.println("📋 线程优先级设置：");
        System.out.println("  " + lowPriority.getName() + " 优先级: " + lowPriority.getPriority());
        System.out.println("  " + normalPriority.getName() + " 优先级: " + normalPriority.getPriority());
        System.out.println("  " + highPriority.getName() + " 优先级: " + highPriority.getPriority());
        
        // 启动线程
        System.out.println("\n🚀 启动不同优先级的线程...");
        highPriority.start();
        normalPriority.start();
        lowPriority.start();
        
        // 等待完成
        try {
            highPriority.join();
            normalPriority.join();
            lowPriority.join();
            System.out.println("\n✅ 优先级演示完成");
        } catch (InterruptedException e) {
            System.err.println("❌ 优先级演示被中断");
        }
    }
    
    /**
     * 显示线程创建方法总结
     * 对比继承Thread与其他创建方式的优缺点
     */
    public static void printMethodSummary() {
        System.out.println("\n" + padEnd("? 继承Thread类方式总结", 50, " "));
        System.out.println(repeat("-", 50));
        System.out.println("✅ 优点:");
        System.out.println("  • 代码结构清晰，易于理解");
        System.out.println("  • 可以直接使用Thread类的方法");
        System.out.println("  • 适合简单的线程创建需求");
        System.out.println("\n❌ 缺点:");
        System.out.println("  • Java单继承限制，无法继承其他类");
        System.out.println("  • 线程代码与Thread类耦合度高");
        System.out.println("  • 不够灵活，复用性较差");
        System.out.println("\n💡 适用场景:");
        System.out.println("  • 简单的线程任务");
        System.out.println("  • 线程逻辑相对独立");
        System.out.println("  • 不需要继承其他类的场景");
    }
}