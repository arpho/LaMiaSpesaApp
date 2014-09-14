package com.eximia.lamiaspesaapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.eximia.lamiaspesaapp.R;
import com.eximia.lamiaspesaapp.utility.Util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
 * contain this fragment must implement the
 * {@link ProductFragment.OnFragmentInteractionListener} interface to handle
 * interaction events. Use the {@link ProductFragment#newInstance} factory
 * method to create an instance of this fragment.
 */
public class ProductFragment extends Fragment {
	private Bitmap thumbImg;
	private View view;

	private class GetItemThumb extends AsyncTask<String, Void, String> {

		protected void onPostExecute(String result) {
			setPicture(view, R.id.immagine_prodotto, thumbImg);
		}

		@Override
		protected String doInBackground(String... thumbURLs) {
			BufferedInputStream thumBuff = null;
			InputStream thumbIn = null;
			try {
				// try to download
				URL thumbURL = new URL(thumbURLs[0]);
				URLConnection thumbConn = thumbURL.openConnection();
				thumbConn.connect();
				thumbIn = thumbConn.getInputStream();
				thumBuff = new BufferedInputStream(thumbIn);
				thumbImg = BitmapFactory.decodeStream(thumBuff);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (thumBuff != null)
					try {
						thumBuff.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				if (thumbIn != null)
					try {
						thumbIn.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
			return "";
		}
		// get thumbnail
	}

	private static final String TAG = ProductFragment.class.getSimpleName();
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "content";
	private static final String ARG_PARAM2 = "format";
	private TextView formatTxt, contentTxt, sviluppoText;
	private ImageView thumbView;

	/*
	 * public void setFormatTxt(String formatTxt) {
	 * this.formatTxt.setText(formatTxt); TextView textView = (TextView)
	 * getView().findViewById(R.id.scan_format); textView.setText(formatTxt); }
	 */
	/*
	 * public void setContentTxt(String contentTxt) { TextView textView =
	 * (TextView) getView().findViewById(R.id.scan_content);
	 * textView.setText(contentTxt); }
	 */

	// TODO: Rename and change types of parameters
	private String Param1;
	private String Param2;

	private OnFragmentInteractionListener mListener;

	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * 
	 * @param param1
	 *            Parameter 1.
	 * @param param2
	 *            Parameter 2.
	 * @return A new instance of fragment ProductFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static ProductFragment newInstance(String param1, String param2) {
		ProductFragment fragment = new ProductFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	public ProductFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			Param1 = getArguments().getString(ARG_PARAM1);
			Param2 = getArguments().getString(ARG_PARAM2);
		}
	}

	private void setText(View view, int id, String txt) {
		TextView Txt = (TextView) view.findViewById(id);
		Txt.setText(txt);

	}

	/*
	 * private void setRating(Double rate){ RatingBar bar = (RatingBar)
	 * view.findViewById(R.id.ratingBar); bar.setIsIndicator(true);
	 * bar.setNumStars((int)Math.round(rate)); }
	 */

	private View setValidData(View view) {
		setText(view, R.id.testo_prodotto,
				this.getArguments().getString("nome_prodotto"));
		setText(view, R.id.testo_descrizione,
				this.getArguments().getString("descrizione"));
		setPicture(view, R.id.immagine_prodotto,
				this.getArguments().getString("picture"));
		setText(view, R.id.mipiace, this.getArguments().getInt("mipiace"));
		setText(view, R.id.nonmipiace, this.getArguments().getInt("nonmipiace"));
		// setText(view, R.id.testo_sviluppo,
		// this.getArguments().getString("developing"));
		/*
		 * Double r = this.getArguments().getDouble("rate"); setRating(r);
		 */
		return view;
	}

	private void setText(View view2, int id, int value) {
		TextView Txt = (TextView) view2.findViewById(id);
		Txt.setText(value+"");

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		view = inflater.inflate(R.layout.fragment_product, container, false);
		if (this.getArguments().getBoolean("hasResult"))
			view = setValidData(view);
		else
			view = setNoResult(view);
		return view;
	}

	private View setNoResult(View view) {
		setText(view, R.id.testo_prodotto, "");
		setText(view, R.id.testo_descrizione, "");
		setPicture(view, R.id.immagine_prodotto,
				this.getArguments().getString("picture"));
		// setText(view, R.id.scan_format,
		// this.getArguments().getString("format"));
		// setText(view, R.id.testo_sviluppo,
		// this.getArguments().getString("developing"));
		// setText(view, R.id.scan_content,
		// this.getArguments().getString("content"));
		return view;
	}

	private void setPicture(View view, int id, String picture) {
		ImageView img = (ImageView) view.findViewById(id);
		if (picture.isEmpty() || picture == null)
			img.setImageResource(R.drawable.noimage);
		else {
			String pictureUrl = generaUrl(picture);
			new GetItemThumb().execute(pictureUrl);
		}
	}

	private String generaUrl(String picture) {
		// TODO inserire la giusta richiesta per le immagini

		return Util.getBaseUrl() + "/media/pics/" + picture;
	}

	private void setPicture(View view, int id, Bitmap picture) {
		ImageView img = (ImageView) view.findViewById(id);
		img.setImageBitmap(picture);
		// TODO caso in cui il server ha un immagine
	}

	// TODO: Rename method, update argument and hook method into UI event
	public void onButtonPressed(Uri uri) {
		if (mListener != null) {
			mListener.onFragmentInteraction(uri);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated to
	 * the activity and potentially other fragments contained in that activity.
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		public void onFragmentInteraction(Uri uri);
	}

}
