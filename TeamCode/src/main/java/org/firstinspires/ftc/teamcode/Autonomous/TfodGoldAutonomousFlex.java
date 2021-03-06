package org.firstinspires.ftc.teamcode.Autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.GlobalVariables;

@Autonomous(name = "Tfod Gold Autonomous Flex", group = "TfodAutonomous")
public class TfodGoldAutonomousFlex extends Nav_Routines {

    int LCR;
    int mineralheading = 7;
    int mineraldistance = 4;
    double distancetraveledtodepot = 0;

    @Override
    public void runOpMode() throws InterruptedException {
        Nav_Init();
        LCR = checktfod2();
        winchup();
        go_sideways(270,0,.3,4);
        go_forward(4,0,.4,false);
        winchdown();

        if (LCR == 1){
            mineralheading = 340;
            mineraldistance = 6;
        } else if (LCR == 3){
            mineralheading = 30;
            mineraldistance = 8;
        }
        turn_to_heading(mineralheading);
        mineralintakeservo.setPower(-.88);
        deploymineralarm();
        extendmineralarm(GlobalVariables.LIFT_DROP_TICKS / 3);
        go_forward(mineraldistance, mineralheading, .3,false);
        raisemineralarm();
        go_forward(mineraldistance + 3, mineralheading, -.3, false);
        turn_to_heading(350);
        dumpmineral(false);
        mineralintakeservo.setPower(0);
        go_forward(14.5,0,.4,false);
        gosidewaysretract(270,0, .55, 30);
        turn_to_heading(225);
        // go to depot
        go_sideways_to_wall(225,.45,3.5, false);
        wallfollow(20, 225, -.45, 3.5, false, false);
        distancetraveledtodepot = wallfollow(14, 225, -.35, 3.5, false, true);

        //drop the marker
        deploymarker2();

        // go to crater
        wallfollow(40 - distancetraveledtodepot, 225, .45, 3.5, false, false);
        go_forward(14, 225, .4, true);

        mineralintakeservo.setPower(-.88);
        deploymineralarm();
        extendmineralarm(GlobalVariables.LIFT_DROP_TICKS / 3);
    }
}
