package app.apptesttask.mvp.presenter;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.arellomobile.mvp.InjectViewState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import app.apptesttask.app.MarvelService;
import app.apptesttask.application.MyApplication;
import app.apptesttask.mvp.models.LocalData;
import app.apptesttask.mvp.models.heroes.Character;
import app.apptesttask.mvp.view.HeroesListTabFragmentView;
import app.apptesttask.util.Constants;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@InjectViewState
public class HeroesListTabFragmentPresenter extends BasePresenter<HeroesListTabFragmentView> {

    @Inject
    MarvelService marvelService;

    private boolean isCompl = true;

    public HeroesListTabFragmentPresenter() {
        MyApplication.getAppComponent().inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        loadHeroesList();
    }

    public void readFile(View view){
        if(!isCompl){
            return;
        }
        Context context = view.getContext();

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(Constants.FILENAME);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (IOException e) {
            onLoadingFailed(e);
        }

        convertToClass(ret);

        isCompl = false;
    }

    private void convertToClass(String str) {

        try{
            JSONObject jsonObject = new JSONObject(str);

            Log.d(Constants.LIKES_ID, "Favorites");

            JSONArray likesJsonArray = jsonObject.getJSONArray(Constants.LIKES_ID);

            for (int i = 0; i < likesJsonArray.length(); i++){
                LocalData.mLikesId.add(likesJsonArray.getInt(i));
                Log.d("schoolClass", String.valueOf(likesJsonArray.getInt(i)));
            }

        }catch (JSONException e)
        {
            onLoadingFailed(e);
        }
    }

    private void loadHeroesList() {
        unsubscribeOnDestroy(marvelService.getHeroesList(String.valueOf(Constants.TS), Constants.PUBLIC_KEY, Constants.HASH)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> showProgress())
                .subscribe(characterDataWrapper -> {
                            List<Character> characters = new ArrayList<>(characterDataWrapper.getData().getResults());
                            onLoading(characters);
                            hideProgress();
                        },
                        error -> {
                            onLoadingFailed(error);
                            hideProgress();
                        })
        );
    }

    private void onLoading(List<Character> characters){
          getViewState().showHeroesList(characters);
    }

    private void hideProgress(){
        getViewState().hideProgress();
    }

    private void onLoadingFailed(Throwable error){
        getViewState().showError(error.toString());
    }

    private void showProgress(){
        getViewState().showProgress();
    }

    public void refreshCalled() {
        loadHeroesList();
    }
}
