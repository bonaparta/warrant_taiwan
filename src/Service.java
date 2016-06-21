class Service {
	public Service(Combiner ec) {
		helper = new Crawler();
		combiner = ec;
	}

	public void accept(final String source, final String destination, final String format) {
		Thread t = new Thread() {
			public void run() {
				helper.process(source, destination, format);
			};
			public void addThread(Thread thread_add) {
				combiner.addThread(thread_add);
			}
		};
		combiner.addThread(t);
		t.start();
	}

	public void accept(final String source, final String destination) {
		Thread t = new Thread() {
			public void run() {
				helper.process(source, destination);
			};
			public void addThread(Thread thread_add) {
				combiner.addThread(thread_add);
			}
		};
		combiner.addThread(t);
		t.start();
	}

	private Crawler helper;
	private Combiner combiner;
}
