package com.pathfinder.app.ui.screens.assessment

import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pathfinder.app.data.model.*
import com.pathfinder.app.ui.components.*
import com.pathfinder.app.ui.theme.DashboardColors.*


@Composable fun AssessmentScreen(pad:PaddingValues,onBack:()->Unit,notify:(String)->Unit,onProfile:()->Unit,onCompleted:(String,List<String>)->Unit){

     val questions=listOf(
      "What kind of activity sounds most interesting?" to listOf("Designing something people can use","Solving a technical problem",
        "Helping a community","Exploring how nature works"),
      "Which strength feels most like you?" to listOf("Creative thinking","Logical problem solving","Communication",
        "Research and curiosity"),
      "Where would you prefer to work?" to listOf("A creative studio","A technology team","With people and communities",
        "In a lab or outdoors"),
      "What matters most in your future career?" to listOf("Making useful experiences","Building new technology",
        "Making a positive difference","Discovering new ideas"),
      "Which subject would you choose to learn more about?" to listOf("Design","Computing","People and society",
        "Science")
     )
     val index=remember {
         mutableStateOf(0)
    }
    val selected=remember {
         mutableStateOf("")
    }
    val result=remember {
         mutableStateOf(false)
    }
    val answers=remember { androidx.compose.runtime.mutableStateListOf<String>() }

     Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).background(Canvas).padding(horizontal=17.dp,
        vertical=12.dp)){

          DashboardHeader("Career Quiz",onBack=onBack,onProfile=onProfile)
        Spacer(Modifier.height(17.dp))
          if(result.value){

               Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp),color=Color.White,border=BorderStroke(1.dp,
                Line)){

                    Column(Modifier.padding(18.dp),horizontalAlignment=Alignment.CenterHorizontally){
                    FeatureIcon("assessment")
                    Text("Your career snapshot",Modifier.padding(top=10.dp),color=Ink,fontSize=20.sp,fontWeight=FontWeight.ExtraBold)
                    Text("Your answers show a mix of curiosity, problem solving, and people focused strengths.",
                        Modifier.padding(top=7.dp),color=Muted,fontSize=12.sp,lineHeight=18.sp)
                    Label("Suggested paths: UX Design · Data Science · Environmental Science",Color(0xFFE8EDFF),
                        Blue)
                    Button(onClick=onBack,modifier=Modifier.fillMaxWidth().padding(top=14.dp),shape=RoundedCornerShape(11.dp)){
                        Text("Back to Home")
                    }
                }

            }

        }
        else{

               Label("QUESTION ${index.value+1} OF ${questions.size}",Color(0xFFE8EDFF),Blue)
               LinearProgressIndicator(progress={
                (index.value+1).toFloat()/questions.size
            },modifier=Modifier.fillMaxWidth().padding(top=12.dp),color=Blue,trackColor=Line)
               Text(questions[index.value].first,Modifier.padding(top=18.dp,bottom=12.dp),color=Ink,fontSize=21.sp,
                fontWeight=FontWeight.ExtraBold)
               questions[index.value].second.forEach {
                 answer->Surface(Modifier.fillMaxWidth().padding(bottom=8.dp).clickable{
                    selected.value=answer
                },shape=RoundedCornerShape(11.dp),color=if(selected.value==answer) Color(0xFFE8EDFF) else Color.White,
                    border=BorderStroke(1.dp,if(selected.value==answer) Blue else Line)){

                        Text(answer,Modifier.fillMaxWidth().padding(horizontal=13.dp,vertical=13.dp),color=if(selected.value==answer) Blue else Ink,
                        fontSize=12.sp,fontWeight=FontWeight.SemiBold)
                }
            }

               Button(onClick={
                if(selected.value.isBlank())notify("Please answer all questions before continuing.") else {
                    answers.add(selected.value)
                    if(index.value==questions.lastIndex) {
                        result.value=true
                        onCompleted("Your answers show a mix of curiosity, problem solving, and people-focused strengths.",answers.toList())
                    } else {
                        index.value++
                        selected.value=""
                    }
                }
            },modifier=Modifier.fillMaxWidth().padding(top=8.dp),shape=RoundedCornerShape(11.dp)){
                Text(if(index.value==questions.lastIndex)"See my results" else "Continue")
            }

        }

    }

}
