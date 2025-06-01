package com.joyzl.webserver.webdav;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.joyzl.webserver.webdav.elements.ActiveLock;
import com.joyzl.webserver.webdav.elements.LockScope;

/**
 * WEBDAV 文件锁
 * 
 * @author ZhangXi 2025年5月30日
 */
public class Locks {

	private final ReentrantLock k = new ReentrantLock(true);
	private final Map<String, ActiveLock> LOCKS = new HashMap<>();
	private final Map<Path, ActiveLock[]> PATHS = new HashMap<>();

	/** 临时数组，同时也是每个资源可创建锁的上限 */
	private final ActiveLock[] TEMP = new ActiveLock[64];

	/** 添加指定资源的锁 */
	public boolean add(Path path, ActiveLock lock) {
		k.lock();
		try {
			ActiveLock[] locks = PATHS.get(path);
			if (locks == null) {
				PATHS.put(path, new ActiveLock[] { lock });
			} else {
				int k = 0, t = 0;
				for (; k < locks.length; k++) {
					if (locks[k].valid()) {
						TEMP[t++] = locks[k];
						if (locks[k].getLockScope() == LockScope.EXCLUSIVE) {
							return false;
						}
					} else {
						LOCKS.remove(locks[k].getLockToken());
					}
				}
				if (t > 0) {
					if (lock.getLockScope() == LockScope.EXCLUSIVE) {
						if (t < k) {
							PATHS.put(path, Arrays.copyOf(TEMP, t));
						}
						return false;
					} else {
						if (t < TEMP.length) {
							TEMP[t++] = lock;
							PATHS.put(path, Arrays.copyOf(TEMP, t));
						} else {
							PATHS.put(path, Arrays.copyOf(TEMP, t));
							return false;
						}
					}
				} else {
					PATHS.put(path, new ActiveLock[] { lock });
				}
			}
			lock.path(path);
			LOCKS.put(lock.getLockToken(), lock);
			return true;
		} finally {
			k.unlock();
		}
	}

	/** 资源是否锁定 */
	public boolean lock(Path path) {
		k.lock();
		try {
			ActiveLock[] locks = PATHS.get(path);
			if (locks != null) {
				int i = 0, t = 0;
				for (; i < locks.length; i++) {
					if (locks[i].valid()) {
						TEMP[t++] = locks[i];
					} else {
						LOCKS.remove(locks[i].getLockToken());
					}
				}
				if (t > 0) {
					if (t < i) {
						PATHS.put(path, locks = Arrays.copyOf(TEMP, t));
					}
					return true;
				} else {
					PATHS.remove(path);
				}
			}
			return false;
		} finally {
			k.unlock();
		}
	}

	/** 获取指定标识的锁 */
	public ActiveLock get(String token) {
		k.lock();
		try {
			ActiveLock lock = LOCKS.get(token);
			if (lock != null) {
				if (!lock.valid()) {
					LOCKS.remove(token);
				}
			}
			return lock;
		} finally {
			k.unlock();
		}
	}

	/** 获取资源的锁 */
	public ActiveLock[] get(Path path) {
		k.lock();
		try {
			ActiveLock[] locks = PATHS.get(path);
			while (locks == null) {
				if ((path = path.getParent()) == null) {
					return null;
				}
				locks = PATHS.get(path);
			}

			int i = 0, t = 0;
			for (; i < locks.length; i++) {
				if (locks[i].valid()) {
					TEMP[t++] = locks[i];
				} else {
					LOCKS.remove(locks[i].getLockToken());
				}
			}
			if (t > 0) {
				if (t < i) {
					PATHS.put(path, locks = Arrays.copyOf(TEMP, t));
				}
			} else {
				PATHS.remove(path);
				locks = null;
			}
			return locks;
		} finally {
			k.unlock();
		}
	}

	/** 获取资源的锁，向上查找至根 */
	public ActiveLock[] get(Path root, Path path) {
		k.lock();
		try {
			ActiveLock[] locks = PATHS.get(path);
			while (locks == null) {
				path = path.getParent();
				if (path == null || path.equals(root)) {
					return null;
				}
				locks = PATHS.get(path);
			}

			int i = 0, t = 0;
			for (; i < locks.length; i++) {
				if (locks[i].valid()) {
					TEMP[t++] = locks[i];
				} else {
					LOCKS.remove(locks[i].getLockToken());
				}
			}
			if (t > 0) {
				if (t < i) {
					PATHS.put(path, locks = Arrays.copyOf(TEMP, t));
				}
			} else {
				PATHS.remove(path);
				locks = null;
			}
			return locks;
		} finally {
			k.unlock();
		}
	}

	/** 移除资源上的所有锁 */
	public ActiveLock[] remove(Path path) {
		return PATHS.remove(path);
	}

	/** 移除资源上的指定锁 */
	public ActiveLock remove(String token) {
		k.lock();
		try {
			final ActiveLock lock = LOCKS.remove(token);
			if (lock != null) {
				ActiveLock[] locks = PATHS.get(lock.path());
				if (locks != null) {
					int k = 0, t = 0;
					for (; k < locks.length; k++) {
						if (locks[k] != lock && locks[k].valid()) {
							TEMP[t++] = locks[k];
						}
					}
					if (t > 0) {
						PATHS.put(lock.path(), Arrays.copyOf(TEMP, t));
					} else {
						PATHS.remove(lock.path());
					}
				}
			}
			return lock;
		} finally {
			k.unlock();
		}
	}
}