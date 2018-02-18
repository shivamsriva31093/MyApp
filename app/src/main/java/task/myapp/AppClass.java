package task.myapp;

import android.app.Application;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.bloco.faker.Faker;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AppClass extends Application {
    private final List<WeakReference<FakerReadyListener>> mListeners = new ArrayList<>();
    private Faker mFaker;

    @Override
    public void onCreate() {
        super.onCreate();
        initFaker();
    }

    public void addListener(@NonNull FakerReadyListener listener) {
        mListeners.add(new WeakReference<>(listener));
        if (mFaker != null) {
            listener.onFakerReady(mFaker);
        }
    }

    public void removeListener(FakerReadyListener listener) {
        for (WeakReference<FakerReadyListener> wRef : mListeners) {
            if (wRef.get() == listener) {
                mListeners.remove(wRef);
                break;
            }
        }
    }

    private void initFaker() {
        Single.create((SingleOnSubscribe<Faker>) e -> {
            final Faker faker = new Faker();

            if (!e.isDisposed()) {
                e.onSuccess(faker);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(faker -> {
                    mFaker = faker;

                    for (WeakReference<FakerReadyListener> wRef : mListeners) {
                        final FakerReadyListener listener = wRef.get();
                        if (listener != null) {
                            listener.onFakerReady(mFaker);
                        }
                    }
                });
    }

    public interface FakerReadyListener {
        void onFakerReady(Faker faker);
    }
}
