package com.example.a7minutesworkout

import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.a7minutesworkout.databinding.ActivityExerciseBinding
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class ExerciseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private var binding : ActivityExerciseBinding? = null

    private var restTimer: CountDownTimer? = null //how many seconds left in the timer
    private var restProgress = 0 //how many seconds has passed

    private var exerciseTimer:CountDownTimer?=null
    private var exerciseProgress=0

    private var exerciseList: ArrayList<ExerciseModel>? = null
    private var currentExcisePosition = -1

    private var tts: TextToSpeech? = null

    private var player: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        exerciseList = Constants.defaultExerciseList()
        tts = TextToSpeech(this, this )
        setBackActionBar()
        setUpRestView()
    }

    private fun setUpRestView(){
        try{
            val soundURI = Uri.parse("android.resource://com.example.a7minutesworkout/"+R.raw.press_start)
            player = MediaPlayer.create(applicationContext, soundURI)
            player?.isLooping = false
            player?.start()
        }catch(e: Exception){
            e.printStackTrace()
            Log.e("restSound", "$e")
        }
        if(restTimer!=null){
            restTimer?.cancel()
            restProgress = 0
        }
        binding?.tvExerciseName?.text =exerciseList?.get(currentExcisePosition+1)!!.getName()
        binding?.tvTitle?.text = "Get Ready For"
        binding?.exerciseImage?.setVisibility(View.INVISIBLE)
        binding?.tvExerciseName?.setVisibility(View.VISIBLE)
        binding?.tvUpcomingExercise?.setVisibility(View.VISIBLE)
        setRestProgressBar()
    }
    private fun setUpExerciseView(){
        if(exerciseTimer!=null){
            exerciseTimer?.cancel()
            exerciseProgress = 0
        }

        val curExercise = exerciseList?.get(currentExcisePosition)

        speakOut(curExercise!!.getName())
        binding?.tvExerciseName?.setVisibility(View.INVISIBLE)
        binding?.tvUpcomingExercise?.setVisibility(View.INVISIBLE)
        binding?.exerciseImage?.setImageResource(curExercise!!.getImage())
        binding?.exerciseImage?.setVisibility(View.VISIBLE)
        binding?.tvTitle?.text = curExercise!!.getName()

        setExerciseProgressBar()
    }

    private fun setRestProgressBar(){
        binding?.progressBar?.progress = restProgress

        //start of timer: every countDownInterval -> do something
        //milisInFuture = total milli second to run the timer
        restTimer = object: CountDownTimer(10000, 1000){
            override fun onTick(p0: Long) {
                restProgress+=1
                binding?.progressBar?.progress = 10 - restProgress
                binding?.tvTimer?.text = (10-restProgress).toString()

            }
            override fun onFinish() {
                currentExcisePosition+=1
                binding?.flProgressBar?.setVisibility(View.INVISIBLE)
                binding?.flProgressBarEx?.setVisibility(View.VISIBLE)
                setUpExerciseView()
            }
        }.start() //start the timer!!!
    }
    private fun setExerciseProgressBar(){
        binding?.progressBarEx?.progress = restProgress

        //start of timer: every countDownInterval -> do something
        //milisInFuture = total milli second to run the timer
        exerciseTimer = object: CountDownTimer(30000, 1000){
            override fun onTick(p0: Long) {
                exerciseProgress+=1
                binding?.progressBarEx?.progress = 30 - exerciseProgress
                binding?.tvTimerEx?.text = (30-exerciseProgress).toString()
            }
            override fun onFinish() {
                if(currentExcisePosition + 1 == exerciseList?.size){
                    Toast.makeText(this@ExerciseActivity, "7 minutes of exercise is completed!", Toast.LENGTH_SHORT).show()
                }else{
                    binding?.flProgressBarEx?.setVisibility(View.INVISIBLE)
                    binding?.flProgressBar?.setVisibility(View.VISIBLE)
                    setUpRestView()
                }

            }
        }.start() //start the timer!!!
    }
    private fun setBackActionBar(){
        setSupportActionBar(binding?.toolBarExercise)
        if(supportActionBar!=null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        binding?.toolBarExercise?.setNavigationOnClickListener{
            onBackPressed()
        }
    }
    override fun onDestroy() {
        super.onDestroy()

        if(restTimer!=null){
            restTimer?.cancel()
            restProgress = 0
        }

        if(exerciseTimer!=null){
            exerciseTimer?.cancel()
            exerciseProgress = 0
        }

        if(tts!=null){
            tts!!.stop()
            tts!!.shutdown()
        }
        if(player!=null){
            player!!.stop()
        }
        binding = null
    }

    override fun onInit(status: Int) {

        if(status == TextToSpeech.SUCCESS){
            val result = tts?.setLanguage(Locale.US)
            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("TTS", "The language specified is not supported!")
            }
        }else{
            Log.e("TTS", "Initialization failed!")
        }
    }
    private fun speakOut(text: String){
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }
}