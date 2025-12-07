package com.soul.synchronizer

import java.util.concurrent.locks.AbstractQueuedSynchronizer

/**
 * @auther: soulagoni
 * @Date:   2025/12/4
 * @Detail:
 */
class SynchronizerTest: AbstractQueuedSynchronizer() {
    /**
     * Attempts to set the state to reflect a release in exclusive
     * mode.
     *
     *
     * This method is always invoked by the thread performing release.
     *
     *
     * The default implementation throws
     * [UnsupportedOperationException].
     *
     * @param arg the release argument. This value is always the one
     * passed to a release method, or the current state value upon
     * entry to a condition wait.  The value is otherwise
     * uninterpreted and can represent anything you like.
     * @return `true` if this object is now in a fully released
     * state, so that any waiting threads may attempt to acquire;
     * and `false` otherwise.
     * @throws IllegalMonitorStateException if releasing would place this
     * synchronizer in an illegal state. This exception must be
     * thrown in a consistent fashion for synchronization to work
     * correctly.
     * @throws UnsupportedOperationException if exclusive mode is not supported
     */
    override fun tryRelease(arg: Int): Boolean {
        return super.tryRelease(arg)
    }

    /**
     * Attempts to acquire in shared mode. This method should query if
     * the state of the object permits it to be acquired in the shared
     * mode, and if so to acquire it.
     *
     *
     * This method is always invoked by the thread performing
     * acquire.  If this method reports failure, the acquire method
     * may queue the thread, if it is not already queued, until it is
     * signalled by a release from some other thread.
     *
     *
     * The default implementation throws [ ].
     *
     * @param arg the acquire argument. This value is always the one
     * passed to an acquire method, or is the value saved on entry
     * to a condition wait.  The value is otherwise uninterpreted
     * and can represent anything you like.
     * @return a negative value on failure; zero if acquisition in shared
     * mode succeeded but no subsequent shared-mode acquire can
     * succeed; and a positive value if acquisition in shared
     * mode succeeded and subsequent shared-mode acquires might
     * also succeed, in which case a subsequent waiting thread
     * must check availability. (Support for three different
     * return values enables this method to be used in contexts
     * where acquires only sometimes act exclusively.)  Upon
     * success, this object has been acquired.
     * @throws IllegalMonitorStateException if acquiring would place this
     * synchronizer in an illegal state. This exception must be
     * thrown in a consistent fashion for synchronization to work
     * correctly.
     * @throws UnsupportedOperationException if shared mode is not supported
     */
    override fun tryAcquireShared(arg: Int): Int {
        return super.tryAcquireShared(arg)
    }

    /**
     * Attempts to set the state to reflect a release in shared mode.
     *
     *
     * This method is always invoked by the thread performing release.
     *
     *
     * The default implementation throws
     * [UnsupportedOperationException].
     *
     * @param arg the release argument. This value is always the one
     * passed to a release method, or the current state value upon
     * entry to a condition wait.  The value is otherwise
     * uninterpreted and can represent anything you like.
     * @return `true` if this release of shared mode may permit a
     * waiting acquire (shared or exclusive) to succeed; and
     * `false` otherwise
     * @throws IllegalMonitorStateException if releasing would place this
     * synchronizer in an illegal state. This exception must be
     * thrown in a consistent fashion for synchronization to work
     * correctly.
     * @throws UnsupportedOperationException if shared mode is not supported
     */
    override fun tryReleaseShared(arg: Int): Boolean {
        return super.tryReleaseShared(arg)
    }

    /**
     * Returns `true` if synchronization is held exclusively with
     * respect to the current (calling) thread.  This method is invoked
     * upon each call to a [ConditionObject] method.
     *
     *
     * The default implementation throws [ ]. This method is invoked
     * internally only within [ConditionObject] methods, so need
     * not be defined if conditions are not used.
     *
     * @return `true` if synchronization is held exclusively;
     * `false` otherwise
     * @throws UnsupportedOperationException if conditions are not supported
     */
    override fun isHeldExclusively(): Boolean {
        return super.isHeldExclusively()
    }

    /**
     * Attempts to acquire in exclusive mode. This method should query
     * if the state of the object permits it to be acquired in the
     * exclusive mode, and if so to acquire it.
     *
     *
     * This method is always invoked by the thread performing
     * acquire.  If this method reports failure, the acquire method
     * may queue the thread, if it is not already queued, until it is
     * signalled by a release from some other thread. This can be used
     * to implement method [Lock.tryLock].
     *
     *
     * The default
     * implementation throws [UnsupportedOperationException].
     *
     * @param arg the acquire argument. This value is always the one
     * passed to an acquire method, or is the value saved on entry
     * to a condition wait.  The value is otherwise uninterpreted
     * and can represent anything you like.
     * @return `true` if successful. Upon success, this object has
     * been acquired.
     * @throws IllegalMonitorStateException if acquiring would place this
     * synchronizer in an illegal state. This exception must be
     * thrown in a consistent fashion for synchronization to work
     * correctly.
     * @throws UnsupportedOperationException if exclusive mode is not supported
     */
    override fun tryAcquire(arg: Int): Boolean {
        return super.tryAcquire(arg)
    }
}