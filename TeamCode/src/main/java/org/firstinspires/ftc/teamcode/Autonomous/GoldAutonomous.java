package org.firstinspires.ftc.teamcode.Autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.GlobalVariables;

@Autonomous(name = "Gold Autonomous", group = "Autonomous")
public class GoldAutonomous extends Nav_Routines {
    double timegoingsideways;

    boolean alphafound;
    boolean yellowfound;

    @Override
    public void runOpMode() throws InterruptedException {
        Nav_Init();
        winchdown();
        go_forward(3, 0, -.2, false, false, 0);
        go_sideways(90, 0, .25, 2, false);
        go_forward(18, 0, -.3, false, false, 0);
        basealpha = (mineralcs.alpha());
        basealpha2 = (mineralcs2.alpha());
        alphafound = go_forward(8, 0, .2, false, true, 0);

        if (alphafound) {
            timegoingsideways = go_sideways(90, 0, .3, 1.5, true);
            yellowfound = mineralknock();
            go_sideways(270, 0, .3, timegoingsideways, false);
        }

        if (yellowfound) {
            go_forward(46, 0, .35, false, false, 0);
        } else {
            alphafound = go_forward(20, 0, .2, false, true, 6);
            if (alphafound) {
                timegoingsideways = go_sideways(90, 0, .3, 1.5, true);
                yellowfound = mineralknock();
                go_sideways(270, 0, .3, timegoingsideways, false);
            }
            if (yellowfound) {
                go_forward(28, 0, .35, false, false, 0);
            } else {
                alphafound = go_forward(20, 0, .2, false, true, 6);
                if (alphafound) {
                    timegoingsideways = go_sideways(90, 0, .3, 1.5, true);
                    yellowfound = mineralknock();
                    go_sideways(270, 0, .2, timegoingsideways, false);
                }
                go_forward(10, 0, .35, false, false, 0);
            }
        }

        turn_to_heading(135);
        go_sideways_to_wall(135,.3,6,true,false);
        // go to depot
        wallfollow(30,135,.35,6, true, false);
        wallfollow(18,135,.2,6, true, false);

        //drop the marker
        deploymarker();

        // go to crater
        wallfollow(47,135,-.35,6, true, true);
        wallfollow(7,135,-.2,6, true, true);
    }
}
