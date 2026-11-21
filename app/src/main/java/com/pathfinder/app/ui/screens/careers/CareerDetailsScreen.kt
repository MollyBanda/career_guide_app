package com.pathfinder.app.ui.screens.careers

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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import com.pathfinder.app.data.model.*
import com.pathfinder.app.ui.components.*
import com.pathfinder.app.ui.theme.DashboardColors.Ink
import com.pathfinder.app.ui.theme.DashboardColors.Muted
import com.pathfinder.app.ui.theme.DashboardColors.Blue
import com.pathfinder.app.ui.theme.DashboardColors.Line
import com.pathfinder.app.ui.theme.DashboardColors.Canvas


private data class CareerProfile(val overview:String,val qualifications:String,val courses:String,val skills:String,
    val pathways:String,val source:String)
private fun careerProfileFor(title:String):CareerProfile? = when {

     title.contains("software developer",true) || title.contains("software engineer",true) -> CareerProfile(
      "Designs, builds, tests, and maintains software applications or systems based on user and business needs.",

          "A bachelor's degree in computer science, software engineering, or a related field is a common route; requirements vary by employer. In Malawi, the University of Malawi lists six MSCE credits including Mathematics, English, and Physics/Physical Science for its BSc Computer Science.",

          "Programming; data structures and algorithms; databases; software design and testing; operating systems and networks.",

          "Programming, analytical thinking, problem solving, communication, teamwork, and attention to detail.",

          "Junior developer to software developer/engineer to senior engineer or technical lead; related paths include QA testing, systems analysis, and app development.",

           "U.S. Bureau of Labor Statistics, Software Developers, QA Analysts, and Testers; University of Malawi Computer Science admissions (check local requirements).")
     title.contains("data scientist",true) -> CareerProfile(
      "Uses statistics, programming, and domain knowledge to analyse data and communicate evidence-based findings.",

          "A bachelor's degree in statistics, mathematics, computer science, business, or engineering is a typical starting point; some roles prefer postgraduate study.",

          "Statistics and probability; calculus/linear algebra; programming; databases; data visualisation; machine learning foundations.",

          "Statistics, analytical and logical thinking, coding, data cleaning, visualisation, problem solving, and communicating results.",

          "Data analyst to junior data scientist to data scientist to specialist or analytics lead; related roles include business intelligence and research analysis.",

           "U.S. Bureau of Labor Statistics, Data Scientists.")
     title.contains("environmental scientist",true) -> CareerProfile(
      "Studies environmental conditions and evidence to help protect ecosystems and human health.",
      "A bachelor's degree in environmental science or a related natural science is a common entry route. Specific requirements vary by role and country.",

          "Environmental science; biology; chemistry; geology; ecology; hydrology; environmental policy; field and laboratory methods.",

          "Scientific research, data analysis, careful fieldwork, report writing, communication, and teamwork.",

          "Field or laboratory assistant to environmental scientist to specialist/consultant or programme lead; possible areas include water, conservation, and impact assessment.",

           "U.S. Bureau of Labor Statistics, Environmental Scientists and Specialists.")
     title.contains("cybersecurity",true) || title.contains("information security",true) -> CareerProfile(
      "Protects computer systems and information by identifying security risks, monitoring for threats, and helping respond to incidents.",

          "A bachelor's degree in computer science, IT, engineering, or a related field is common; relevant IT experience and optional certifications can help. Employers differ.",

          "Computer networks; operating systems; programming/scripting; databases; information security; risk management; incident response.",

          "Analytical thinking, problem solving, careful documentation, communication, networking and systems knowledge.",

          "IT support or network role to security analyst to senior analyst or security engineer to security architect/lead.",

           "U.S. Bureau of Labor Statistics, Information Security Analysts.")
     title.contains("ux designer",true) -> CareerProfile(
      "Researches user needs and designs and evaluates clear, accessible digital experiences.",
      "There is no single required degree. Common routes include interaction design, human-computer interaction, graphic design, or computer science, supported by a portfolio.",

          "User research; interaction and interface design; accessibility; prototyping; usability testing; visual communication.",

          "Empathy, research, visual communication, prototyping, collaboration, and explaining design decisions.",

          "Junior UX/UI designer to product designer or UX researcher to senior/lead designer; related paths include service design and usability research.",

           "Suggested preparation; qualification requirements vary by employer and location.")
     title.contains("community health worker",true) -> CareerProfile(
      "Supports community health by sharing trusted information, connecting people to services, and helping run outreach and prevention programmes.",

          "Requirements differ by programme and country. Some entry roles require secondary education plus workplace training; local health-service rules and approved training must be checked before applying.",

          "Community health; wellness and prevention; communication; ethics and confidentiality; outreach and referral; basic record keeping.",

          "Clear communication, listening, cultural sensitivity, teaching, teamwork, empathy, and practical problem solving.",

          "Community outreach assistant to community health worker to experienced worker, trainer, or programme coordinator (subject to local qualifications and employer requirements).",

           "U.S. Bureau of Labor Statistics, Community Health Workers; World Health Organization, Global Curriculum Guide for Community Health Workers.")
     title.contains("marketing",true) -> CareerProfile(
      "Plans and evaluates ways to understand audiences and communicate the value of products or services.",

          "Business, marketing, communications, or social-science study are common routes. Employers may ask for experience or further study.",

          "Marketing principles; consumer behaviour; statistics; research methods; communications; digital analytics.",

          "Research, analytical and critical thinking, communication, creativity, and attention to detail.",

          "Marketing assistant to marketing or market-research analyst to strategist/manager; related paths include communications and brand management.",

           "U.S. Bureau of Labor Statistics, Market Research Analysts (for research and analytics duties).")
     else -> null
}
@Composable private fun InfoSection(title:String,body:String){

     Column(Modifier.fillMaxWidth().padding(top=12.dp)){
        Text(title,color=Ink,fontSize=13.sp,fontWeight=FontWeight.Bold)
        Text(body.replace('\u2192', ' '),Modifier.padding(top=4.dp),color=Muted,fontSize=11.sp,lineHeight=16.sp)
    }

}
@Composable fun CareerDetailsScreen(pad:PaddingValues,title:String,subtitle:String,category:String,savedTitles:Set<String>,
    onToggleSave:(String)->Unit,onBack:()->Unit,onStartQuiz:()->Unit,onProfile:()->Unit,catalogItems:List<OpportunityEntity>,
    universityProgrammes:List<UniversityProgrammeEntity> = emptyList()){
    val canSave=category in setOf("Careers","Internships","Scholarships","Training","Universities")
    val saved=title in savedTitles
    val storedItem=catalogItems.firstOrNull { it.title.equals(title,ignoreCase=true) }
    val isUniversityProfile=category=="Universities" && storedItem!=null &&
        (storedItem.id=="uni-malawi" || storedItem.id.startsWith("uni-expanded-") || storedItem.title=="University of Malawi")
    var selectedProgramme by remember(title,category) { mutableStateOf<UniversityProgrammeEntity?>(null) }
    val uriHandler=LocalUriHandler.current
    val careerProfile=if(category=="Careers" && storedItem==null) careerProfileFor(title) else null
    val description=storedItem?.overview ?: when(category){
        "Scholarships"->"Review eligibility, application timing, and funding details for this scholarship. Save it to keep it with your opportunities."
        "Assessment"->"Answer a short set of questions to understand your strengths and interests, then use your results to explore suitable pathways."
        "Internships"->"Explore the role, experience you can gain, and the skills expected. Save this opportunity to revisit it later."
        "Training"->"Review the learning outcomes, expected time, and skills you can build through this training option."
        "Profile"->subtitle
        else->"Explore the role, related skills, study options, and career pathways. Save this opportunity to revisit it later."
    }

    Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).background(Canvas).padding(horizontal=16.dp,vertical=12.dp)){
        DashboardHeader(if(selectedProgramme!=null) "Programme Details" else "Details",
            onBack={ if(selectedProgramme!=null) selectedProgramme=null else onBack() },onProfile=onProfile)
        Spacer(Modifier.height(16.dp))
        Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(17.dp),color=MaterialTheme.colorScheme.surface,border=BorderStroke(1.dp,Line)){
            Column(Modifier.padding(16.dp)){
                if(selectedProgramme!=null){
                    val programme=selectedProgramme!!
                    Label(programme.studyLevel.uppercase(),Color(0xFFE8EDFF),Blue)
                    Text(programme.programmeName,Modifier.padding(top=12.dp),color=Ink,fontSize=22.sp,fontWeight=FontWeight.ExtraBold,lineHeight=27.sp)
                    InfoSection("Qualification awarded",programme.award)
                    InfoSection("Programme duration",programme.duration)
                    InfoSection("Entry requirements",programme.entryRequirements)
                    InfoSection("Required school subjects or qualifications",programme.requiredSubjects)
                    InfoSection("Faculty or department",programme.facultyOrDepartment)
                    InfoSection("Study level",programme.studyLevel)
                    InfoSection("Application procedure",programme.applicationProcedure)
                    InfoSection("Last verified",programme.lastVerified + if(programme.verificationNote.isBlank()) "" else "\n${programme.verificationNote}")
                    Text("Official programme and admissions link",color=Ink,fontSize=13.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=12.dp))
                    Text(programme.officialUrl,Modifier.padding(top=4.dp).clickable { runCatching { uriHandler.openUri(programme.officialUrl) } },color=Blue,fontSize=12.sp,lineHeight=18.sp)
                    if(programme.sourceUrl!=programme.officialUrl) Text("Source: ${programme.sourceUrl}",Modifier.padding(top=6.dp).clickable { runCatching { uriHandler.openUri(programme.sourceUrl) } },color=Blue,fontSize=11.sp)
                } else {
                    Label(category.uppercase(),Color(0xFFE8EDFF),Blue)
                    Text(title,Modifier.padding(top=12.dp),color=Ink,fontSize=22.sp,fontWeight=FontWeight.ExtraBold,lineHeight=27.sp)
                    Text(subtitle,Modifier.padding(top=5.dp),color=Muted,fontSize=12.sp)
                    Spacer(Modifier.height(13.dp))
                    Text(description,color=Ink,fontSize=12.sp,lineHeight=19.sp)

                    if(isUniversityProfile){
                        val university=storedItem
                        InfoSection("Country",subtitle.substringBefore(" ·"))
                        InfoSection("City or town",subtitle.substringAfter(" ·","Location not confirmed"))
                        InfoSection("Campus information",university?.overview?.substringAfter("Campus information: ")?.substringBefore(".") ?: "Check the official university website for current campus details.")
                        val linkedProgrammes=universityProgrammes.filter { it.universityId==university?.id }
                        Text("Programmes Offered",Modifier.padding(top=16.dp,bottom=4.dp),color=Ink,fontSize=17.sp,fontWeight=FontWeight.Bold)
                        Text("Only programme records verified for this university are shown here.",color=Muted,fontSize=11.sp,lineHeight=16.sp)
                        if(linkedProgrammes.isEmpty()) Text("No programme details have been verified for this university yet. Check its official programme catalogue for current awards and admissions.",Modifier.padding(top=12.dp),color=Muted,fontSize=12.sp,lineHeight=18.sp)
                        linkedProgrammes.forEach { programme ->
                            Surface(Modifier.fillMaxWidth().padding(top=8.dp).clickable { selectedProgramme=programme }.testTag("university-programme-${programme.id}"),shape=RoundedCornerShape(12.dp),color=MaterialTheme.colorScheme.surfaceVariant,border=BorderStroke(1.dp,Line)){
                                Column(Modifier.padding(12.dp)){
                                    Text(programme.programmeName,color=Ink,fontSize=13.sp,fontWeight=FontWeight.Bold)
                                    Text("${programme.award} · ${programme.duration}",Modifier.padding(top=4.dp),color=Muted,fontSize=11.sp)
                                }
                            }
                        }
                        InfoSection("Admissions",university?.requirements ?: "Check the institution's official admissions page.")
                        InfoSection("Application dates",university?.deadline ?: "Check the current university admissions notice.")
                        Text("Official university website",color=Ink,fontSize=13.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=12.dp))
                        val universityUrl=university?.applicationUrl.orEmpty()
                        if(universityUrl.isBlank()) Text("Official website not confirmed. Check the Malawi National Council for Higher Education institution listing.",Modifier.padding(top=4.dp),color=Muted,fontSize=12.sp)
                        else Text(universityUrl,Modifier.padding(top=4.dp).clickable { runCatching { uriHandler.openUri(universityUrl) } },color=Blue,fontSize=12.sp,lineHeight=18.sp)
                        university?.provider?.substringAfter("Source checked: ")?.let { InfoSection("Last source check",it.substringBefore("\n")) }
                        university?.provider?.substringAfter("Institution note: ")?.takeIf { it!=university.provider }?.let { InfoSection("Institution name note",it) }
                    } else {
                        if(storedItem!=null){
                            if(storedItem.provider.isNotBlank()) InfoSection("Provider and source",storedItem.provider)
                            if(storedItem.qualifications.isNotBlank()) InfoSection("Qualifications",storedItem.qualifications)
                            if(storedItem.courses.isNotBlank()) InfoSection("Courses and learning",storedItem.courses)
                            if(storedItem.skills.isNotBlank()) InfoSection("Skills",storedItem.skills)
                            if(storedItem.pathways.isNotBlank()) InfoSection("Pathways and application",storedItem.pathways)
                            if(storedItem.requirements.isNotBlank()) InfoSection("Requirements and eligibility",storedItem.requirements)
                            if(storedItem.duration.isNotBlank()) InfoSection("Duration",storedItem.duration)
                            if(storedItem.deadline.isNotBlank()) InfoSection("Availability and dates",storedItem.deadline)
                            if(storedItem.applicationUrl.isNotBlank()){
                                Text("Official provider website",color=Ink,fontSize=13.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=12.dp))
                                Text(storedItem.applicationUrl,Modifier.padding(top=4.dp).clickable { runCatching { uriHandler.openUri(storedItem.applicationUrl) } },color=Blue,fontSize=12.sp,lineHeight=18.sp)
                            }
                        }
                        if(careerProfile!=null){
                            InfoSection("Role overview",careerProfile.overview)
                            InfoSection("Qualifications",careerProfile.qualifications)
                            InfoSection("Suggested courses",careerProfile.courses)
                            InfoSection("Skills needed",careerProfile.skills)
                            InfoSection("Career pathways",careerProfile.pathways)
                            InfoSection("Reference",careerProfile.source)
                        }
                        if(category=="Internships"){
                            InfoSection("What you can gain","Practical experience, workplace communication, teamwork, and role-specific skills. Check each host organisation for current eligibility and application dates.")
                            InfoSection("Application checklist","Review the role requirements, prepare a CV and cover letter, and confirm the deadline with the provider.")
                        }
                    }

                    if(canSave){
                        Spacer(Modifier.height(16.dp))
                        Button(onClick={onToggleSave(title)},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(11.dp),colors=ButtonDefaults.buttonColors(containerColor=if(saved) Color(0xFFE8EDFF) else Blue,contentColor=if(saved) Blue else MaterialTheme.colorScheme.onPrimary)){
                            Icon(if(saved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,contentDescription=null,modifier=Modifier.size(17.dp))
                            Spacer(Modifier.width(7.dp))
                            Text(if(saved)"Remove from Saved" else "Save this opportunity",fontSize=12.sp,fontWeight=FontWeight.Bold)
                        }
                    }
                    if(category=="Assessment"){
                        Spacer(Modifier.height(16.dp))
                        Button(onClick=onStartQuiz,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(11.dp)){Text("Start assessment",fontSize=12.sp,fontWeight=FontWeight.Bold)}
                    }
                }
            }
        }
    }
}
