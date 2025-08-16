package valorless.havenbags.utils;

import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Consumer;

import valorless.havenbags.Main;


public class TaskUtils {

	/**
	 * Run something asynchronously, then process its result on the main thread.
	 *
	 * @param asyncSupplier Code that runs asynchronously and returns a result
	 * @param syncConsumer  Code that uses the result on the main thread
	 * @param <T>           The type of result being passed
	 */
	public static <T> BukkitTask runAsyncThenSync(Supplier<T> asyncSupplier, Consumer<T> syncConsumer) {
		return Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
			T result = asyncSupplier.get();
			Bukkit.getScheduler().runTask(Main.plugin, () -> syncConsumer.accept(result));
		});
	}
}