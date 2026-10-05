/*
 * Feature : _Atomic, <stdatomic.h>, <threads.h>, _Thread_local
 * Version : C11
 * Spec    : N1570 6.7.2.4 (atomic type specifier), 6.7.3 (_Atomic qualifier),
 *           6.7.1 (_Thread_local), 7.17 (stdatomic.h), 7.26 (threads.h)
 *
 * C11 added a memory model, atomic types and an optional threads library.
 *
 * Parser edge cases:
 *  - `_Atomic` is BOTH a type specifier `_Atomic(int)` and a type qualifier
 *    `_Atomic int`. With a following `(` it is ALWAYS the specifier form.
 *  - `_Atomic(int) *p` vs `_Atomic int *p` vs `int *_Atomic p` - three
 *    different declarations.
 *  - `_Thread_local` is a storage-class specifier (C23 adds `thread_local`
 *    keyword; here it's a macro from <threads.h>).
 *  - Compound assignment on atomics (`counter += 1`) is atomic RMW.
 *  - __STDC_NO_ATOMICS__ / __STDC_NO_THREADS__ mark these as optional.
 */
#include <stdio.h>
#include <stdbool.h>
#include <stdatomic.h>
#include <threads.h>

#define WORKERS    4
#define ITERATIONS 10000

static _Atomic(long) shared_counter = 0;   /* specifier form */
static _Atomic int   hits = 0;             /* qualifier form */
static atomic_bool   ready = false;
static atomic_flag   winner_flag = ATOMIC_FLAG_INIT;
static _Thread_local int per_thread_calls = 0;
static thread_local  int tl_alias = 0;     /* macro spelling */
static mtx_t print_lock;

static int worker(void *arg)
{
    int id = *(int *)arg;
    while (!atomic_load_explicit(&ready, memory_order_acquire))
        thrd_yield();

    for (int i = 0; i < ITERATIONS; i++) {
        atomic_fetch_add_explicit(&shared_counter, 1, memory_order_relaxed);
        hits += 1;                         /* atomic read-modify-write */
        per_thread_calls++;
        tl_alias++;
    }
    if (!atomic_flag_test_and_set(&winner_flag)) {
        mtx_lock(&print_lock);
        printf("one worker won the atomic_flag\n");
        mtx_unlock(&print_lock);
    }
    mtx_lock(&print_lock);
    printf("worker done: per_thread_calls=%d tl_alias=%d\n",
           per_thread_calls, tl_alias);
    mtx_unlock(&print_lock);
    return id;
}

int main(void)
{
    thrd_t threads[WORKERS];
    int ids[WORKERS];
    mtx_init(&print_lock, mtx_plain);

    for (int i = 0; i < WORKERS; i++) {
        ids[i] = i;
        thrd_create(&threads[i], worker, &ids[i]);
    }
    atomic_store_explicit(&ready, true, memory_order_release);

    int sum_ids = 0;
    for (int i = 0; i < WORKERS; i++) {
        int res;
        thrd_join(threads[i], &res);
        sum_ids += res;
    }

    /* Compare-and-swap loop */
    long expected = atomic_load(&shared_counter);
    while (!atomic_compare_exchange_weak(&shared_counter, &expected, expected * 2))
        ;
    printf("counter doubled = %ld (expected %d)\n",
           atomic_load(&shared_counter), 2 * WORKERS * ITERATIONS);
    printf("hits = %d, sum of ids = %d\n", atomic_load(&hits), sum_ids);
    printf("main thread per_thread_calls = %d (thread-local)\n", per_thread_calls);
    printf("lock-free long: %d\n", atomic_is_lock_free(&shared_counter));
    mtx_destroy(&print_lock);
    return 0;
}
